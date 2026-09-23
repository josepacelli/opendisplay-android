package io.github.josepacelli.opendisplay.video

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Build
import android.view.Surface
import io.github.josepacelli.opendisplay.net.VideoFrame
import io.github.josepacelli.opendisplay.util.Log
import java.nio.ByteBuffer

/**
 * Hardware H.264 decode straight to a [Surface] — no CPU YUV conversion, no
 * custom shader path. Unlike the iOS receiver (which optionally routes
 * through a Metal shader because `AVSampleBufferDisplayLayer` is suspected of
 * an extra buffered frame), `MediaCodec` configured with an output `Surface`
 * already gets a dedicated compositor path, so there is no equivalent
 * trade-off to make here — see CLAUDE.md.
 *
 * The real coded size comes from parsing the SPS itself ([H264Sps]) — some
 * decoders (Qualcomm's C2 AVC decoder, at least) echo `MediaFormat`'s seed
 * size back through `INFO_OUTPUT_FORMAT_CHANGED` instead of the actual
 * bitstream dimensions once cropped, which silently breaks the aspect ratio
 * in Mirror mode (issue #44; Extend mode happened to seed with the right
 * size anyway, so it never showed there). `INFO_OUTPUT_FORMAT_CHANGED` is
 * kept only as a fallback for the rare SPS this parser can't handle.
 * [expectedWidth]/[expectedHeight] (this device's own panel size) still seed
 * `MediaFormat` before the first SPS arrives. MediaCodec expects
 * Annex-B access units in its input buffers on Android (unlike VideoToolbox's
 * AVCC), so wire NALUs are fed through unchanged, just prefixed with start
 * codes.
 *
 * [submit]/[release] aren't thread-safe against each other — feed them from a single
 * thread/coroutine (the same one draining
 * [io.github.josepacelli.opendisplay.net.PhoneReceiver.videoFrames]). Internally, each
 * configured codec gets its own dedicated thread blocking on `dequeueOutputBuffer` so a
 * decoded frame reaches the [Surface] the instant it's ready, instead of waiting for the
 * *next* [submit] to trigger a drain — on a mostly static screen NALUs arrive seconds
 * apart, and that coupling held every decoded frame hostage to the next network packet
 * (issue #113). [onSizeChanged]/[onError] can therefore fire from either the calling
 * thread or that drain thread; both are serialized against each other so a caller never
 * sees them run concurrently.
 *
 * @param surface where decoded frames are rendered.
 * @param expectedWidth seed width in pixels, used until the real size arrives.
 * @param expectedHeight seed height in pixels, used until the real size arrives.
 * @param onSizeChanged called with the real coded size, once known (from the SPS, or the
 * decoder's own output format when the SPS couldn't be parsed).
 * @param onError called (at most once a second) with the running desync count for this
 * decoder instance when the codec needs a fresh keyframe (decoder error, or a gap in
 * [VideoFrame.seq] — frames the receiver's buffer dropped under a burst, e.g. after a WiFi
 * stall) — mirrors the iOS receiver's `requestKeyframeIfNeeded`. Without this, a broken
 * reference chain would otherwise leave the picture garbled/frozen until the Mac's own
 * periodic keyframe, up to 60s away (see `Mac/MacSender.swift`).
 */
class VideoDecoder(
    private val surface: Surface,
    private var expectedWidth: Int,
    private var expectedHeight: Int,
    private val onSizeChanged: (width: Int, height: Int) -> Unit = { _, _ -> },
    private val onError: (desyncCount: Int) -> Unit = {},
) {
    @Volatile private var codec: MediaCodec? = null
    @Volatile private var drainThread: Thread? = null
    private var currentSps: ByteArray? = null
    private var currentPps: ByteArray? = null
    private var pendingSps: ByteArray? = null
    private var pendingPps: ByteArray? = null
    @Volatile private var spsDimensionsKnown = false
    private var lastErrorSignalAt = 0L
    private var lastSeq: Long? = null
    private var justReconfigured = false
    private var desyncCount = 0
    private var lastReconfigureAt = 0L

    /** Update the seed size (e.g. after a rotation) before the next SPS/PPS
     * change triggers a reconfigure. Does not itself force a reconfigure —
     * the wire protocol always follows a rotation with new SPS/PPS anyway.
     * @param width new seed width in pixels.
     * @param height new seed height in pixels.
     */
    fun updateExpectedSize(width: Int, height: Int) {
        expectedWidth = width
        expectedHeight = height
    }

    /** Feeds one wire [VideoFrame] to the decoder, reconfiguring first if it carries new SPS/PPS —
     * throttled to once a second so a peer can't force a reconfigure per frame. A change that
     * arrives inside the throttle window stays pending (not dropped) and gets applied on the
     * first `submit()` after the window passes, even if that frame doesn't itself carry new
     * SPS/PPS — otherwise the codec could stay desynced from [pendingSps]/[pendingPps] for the
     * rest of the session, since the peer only resends headers when they change again.
     * @param frame the frame to decode. */
    fun submit(frame: VideoFrame) {
        frame.sps?.let { pendingSps = it }
        frame.pps?.let { pendingPps = it }
        val sps = pendingSps
        val pps = pendingPps
        val headersPending = (sps != null && !sps.contentEquals(currentSps)) ||
            (pps != null && !pps.contentEquals(currentPps))
        var headersChanged = false
        if (headersPending) {
            val now = System.currentTimeMillis()
            if (now - lastReconfigureAt > 1000) {
                lastReconfigureAt = now
                currentSps = sps
                currentPps = pps
                headersChanged = true
                reconfigure()
            }
        }
        val expectedSeq = lastSeq?.plus(1)
        if (!headersChanged && !justReconfigured && expectedSeq != null && frame.seq != expectedSeq) {
            Log.warn("video frame gap (expected seq $expectedSeq, got ${frame.seq}) — requesting keyframe")
            signalDesync()
        }
        justReconfigured = headersChanged
        lastSeq = frame.seq
        if (frame.vclNalus.isEmpty()) return
        val mediaCodec = codec ?: return
        queueAccessUnit(mediaCodec, frame.vclNalus)
    }

    /** Tears down any existing codec and builds a fresh one from [currentSps]/[currentPps]. */
    @Synchronized
    private fun reconfigure() {
        val sps = currentSps ?: return
        val pps = currentPps ?: return
        release()
        val spsDims = H264Sps.parseDimensions(sps)
        spsDimensionsKnown = spsDims != null
        if (spsDims != null) {
            onSizeChanged(spsDims.width, spsDims.height)
        }
        val seedWidth = spsDims?.width ?: expectedWidth
        val seedHeight = spsDims?.height ?: expectedHeight
        try {
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, seedWidth, seedHeight)
            format.setByteBuffer("csd-0", ByteBuffer.wrap(START_CODE + sps))
            format.setByteBuffer("csd-1", ByteBuffer.wrap(START_CODE + pps))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                format.setInteger(MediaFormat.KEY_LOW_LATENCY, 1)
            }
            format.setInteger(MediaFormat.KEY_PRIORITY, 0)

            val mediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            mediaCodec.configure(format, surface, null, 0)
            mediaCodec.start()
            codec = mediaCodec
            startDrainThread(mediaCodec)
            Log.info("MediaCodec configured, seed ${seedWidth}x$seedHeight (sps-derived: $spsDimensionsKnown)")
        } catch (e: Exception) {
            Log.error("MediaCodec configure failed", e)
            signalError()
        }
    }

    private fun startDrainThread(mediaCodec: MediaCodec) {
        val thread = Thread({ drainLoop(mediaCodec) }, "VideoDecoder-drain")
        thread.isDaemon = true
        drainThread = thread
        thread.start()
    }

    /** Writes one access unit's NALUs into an input buffer and submits it.
     * @param mediaCodec the running codec to submit into.
     * @param nalus every NALU belonging to this access unit, in wire order. */
    private fun queueAccessUnit(mediaCodec: MediaCodec, nalus: List<ByteArray>) {
        try {
            val index = mediaCodec.dequeueInputBuffer(0)
            if (index < 0) {
                signalDesync()
                return
            }
            val inputBuffer = mediaCodec.getInputBuffer(index) ?: return
            inputBuffer.clear()
            var size = 0
            for (nalu in nalus) {
                inputBuffer.put(START_CODE)
                inputBuffer.put(nalu)
                size += START_CODE.size + nalu.size
            }
            mediaCodec.queueInputBuffer(index, 0, size, System.nanoTime() / 1000, 0)
        } catch (e: Exception) {
            Log.error("decode failed — rebuilding on the next keyframe", e)
            signalError()
        }
    }

    /** Tears the codec down and forgets the last-seen SPS/PPS, so even a
     * keyframe with byte-identical headers to before triggers a fresh
     * [reconfigure] (headersChanged in [submit] only fires on a *change*).
     * For a codec that's actually broken (threw on configure/queue) —
     * rebuilding is the only way back. */
    @Synchronized
    private fun signalError() {
        release()
        currentSps = null
        currentPps = null
        signalDesync()
    }

    /** The decoder fell out of sync with the encoder's reference chain — a
     * dropped/corrupt access unit, not necessarily a broken codec — so just
     * ask the Mac for a fresh IDR. Debounced to at most once a second: a
     * stuck codec/persistent backlog would otherwise fail every single frame
     * and spam keyframe requests.
     *
     * [desyncCount] resets after a quiet spell ([DESYNC_EPISODE_GAP_MS]) — it counts
     * a run of *recent* trouble, not a lifetime total, so a couple of isolated blips an
     * hour apart never add up to looking like sustained instability. */
    @Synchronized
    private fun signalDesync() {
        val now = System.currentTimeMillis()
        if (now - lastErrorSignalAt <= 1000) return
        if (now - lastErrorSignalAt > DESYNC_EPISODE_GAP_MS) desyncCount = 0
        lastErrorSignalAt = now
        desyncCount++
        onError(desyncCount)
    }

    /** Runs on a dedicated thread for [mediaCodec]'s whole lifetime, blocking on
     * `dequeueOutputBuffer` so a decoded frame reaches the [Surface] the moment it's ready
     * — see the class doc for why this isn't just called inline from [queueAccessUnit]
     * anymore. Exits once [codec] no longer points at [mediaCodec] (torn down by
     * [release]) or the codec throws.
     * @param mediaCodec the codec this thread owns. */
    private fun drainLoop(mediaCodec: MediaCodec) {
        val info = MediaCodec.BufferInfo()
        while (codec === mediaCodec) {
            val outIndex = try {
                mediaCodec.dequeueOutputBuffer(info, DRAIN_TIMEOUT_US)
            } catch (e: Exception) {
                if (codec === mediaCodec) signalError()
                return
            }
            when {
                outIndex >= 0 -> try {
                    mediaCodec.releaseOutputBuffer(outIndex, true)
                } catch (e: Exception) {
                    if (codec === mediaCodec) signalError()
                    return
                }
                outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    val format = mediaCodec.outputFormat
                    val width = format.getInteger(MediaFormat.KEY_WIDTH)
                    val height = format.getInteger(MediaFormat.KEY_HEIGHT)
                    Log.info("decoder output format changed: ${width}x$height")
                    if (!spsDimensionsKnown) onSizeChanged(width, height)
                }
            }
        }
    }

    /** Stops and releases the codec, if one exists — safe to call more than once, and safe
     * to call from [drainLoop]'s own thread (skips self-joining, which would otherwise
     * deadlock). */
    @Synchronized
    fun release() {
        val mc = codec
        codec = null
        val thread = drainThread
        drainThread = null
        if (thread != null && thread !== Thread.currentThread()) {
            try {
                thread.join(DRAIN_JOIN_TIMEOUT_MS)
            } catch (_: InterruptedException) {
            }
        }
        mc?.let {
            try {
                it.stop()
            } catch (_: Exception) {
            }
            try {
                it.release()
            } catch (_: Exception) {
            }
        }
    }

    companion object {
        private val START_CODE = byteArrayOf(0, 0, 0, 1)
        private const val DESYNC_EPISODE_GAP_MS = 5_000L
        private const val DRAIN_TIMEOUT_US = 10_000L
        private const val DRAIN_JOIN_TIMEOUT_MS = 200L
    }
}
