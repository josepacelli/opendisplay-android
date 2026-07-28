package com.peetzweg.opensidecar.android.ui

import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.viewinterop.AndroidView
import com.peetzweg.opensidecar.android.net.PhoneReceiver
import com.peetzweg.opensidecar.android.util.Log
import com.peetzweg.opensidecar.android.video.VideoDecoder
import kotlin.math.abs

private const val TOUCH_SLOP_PX = 24f

/** Decoded frame size, once MediaCodec reports its real output format. */
data class VideoDims(val width: Int, val height: Int)

/**
 * Hosts the `SurfaceView` MediaCodec renders into and turns touch on it into
 * `touch`/`scroll` wire messages. Sized by the caller — [ReceiverScreen] wraps
 * this in an aspect-ratio [androidx.compose.foundation.layout.Box] (video
 * width/height first guessed from this device's own announced panel size,
 * then refined via [onVideoDimsChanged]) so it, and the cursor overlay next
 * to it, both fill that same letterboxed/pillarboxed rect.
 */
@Composable
fun VideoSurface(
    receiver: PhoneReceiver,
    videoDims: VideoDims?,
    onVideoDimsChanged: (VideoDims) -> Unit,
    modifier: Modifier = Modifier,
) {
    var decoder by remember { mutableStateOf<VideoDecoder?>(null) }
    val currentReceiver by rememberUpdatedState(receiver)
    val currentDims by rememberUpdatedState(videoDims)
    val onDimsChanged by rememberUpdatedState(onVideoDimsChanged)

    // Feeds every decoded frame to whichever decoder is currently attached —
    // `decoder` is read live through the closure each emission, so this one
    // collector survives surface create/destroy cycles.
    LaunchedEffect(receiver) {
        receiver.videoFrames.collect { frame -> decoder?.submit(frame) }
    }

    AndroidView(
        modifier = modifier
            .pointerInput(receiver) {
                handleTouchAndScroll(
                    getVideoDims = { currentDims },
                    receiver = currentReceiver,
                )
            },
        factory = { context ->
            SurfaceView(context).apply {
                holder.addCallback(object : SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: SurfaceHolder) {
                        Log.info("SurfaceView created — attaching decoder")
                        decoder = VideoDecoder(
                            surface = holder.surface,
                            expectedWidth = currentReceiver.devicePixelsWide.takeIf { it > 0 } ?: 1280,
                            expectedHeight = currentReceiver.devicePixelsHigh.takeIf { it > 0 } ?: 720,
                            onSizeChanged = { w, h -> onDimsChanged(VideoDims(w, h)) },
                            onError = { currentReceiver.requestKeyframe() },
                        )
                    }

                    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

                    override fun surfaceDestroyed(holder: SurfaceHolder) {
                        Log.info("SurfaceView destroyed — releasing decoder")
                        decoder?.release()
                        decoder = null
                    }
                })
            }
        },
    )

    DisposableEffect(Unit) {
        onDispose { decoder?.release() }
    }
}

/**
 * Single-finger drag -> `touch` (began/moved/ended). A second finger joining
 * before the first has moved past a small slop switches the whole gesture to
 * `scroll` (centroid delta of every active pointer) instead — this avoids
 * ever sending `began` for what turns out to be a two-finger scroll, which
 * would otherwise leave the Mac's mouse button stuck down (see
 * `Mac/InputInjector.swift`: `began` maps straight to `mouseDown`).
 */
private suspend fun PointerInputScope.handleTouchAndScroll(
    getVideoDims: () -> VideoDims?,
    receiver: PhoneReceiver,
) {
    awaitEachGesture {
        val first = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Main)
        val startPos = first.position
        var committedMode: GestureMode = GestureMode.UNDECIDED
        var lastCentroid = startPos

        fun normalized(x: Float, y: Float) = (x / size.width).toDouble().coerceIn(0.0, 1.0) to
            (y / size.height).toDouble().coerceIn(0.0, 1.0)

        while (true) {
            val event = awaitPointerEvent()
            val pressed = event.changes.filter { it.pressed }

            when (committedMode) {
                GestureMode.UNDECIDED -> when {
                    pressed.size >= 2 -> {
                        committedMode = GestureMode.SCROLL
                        lastCentroid = centroidOf(pressed)
                    }

                    pressed.size == 1 -> {
                        val moved = pressed[0].position - startPos
                        if (abs(moved.x) > TOUCH_SLOP_PX || abs(moved.y) > TOUCH_SLOP_PX) {
                            committedMode = GestureMode.TOUCH
                            val (nx, ny) = normalized(first.position.x, first.position.y)
                            receiver.sendTouch("began", nx, ny)
                            val (mx, my) = normalized(pressed[0].position.x, pressed[0].position.y)
                            receiver.sendTouch("moved", mx, my)
                            lastCentroid = pressed[0].position
                        }
                        // else: still inside the slop — stay UNDECIDED and keep waiting.
                    }

                    else -> {
                        // Lifted before crossing slop or gaining a second pointer: a tap.
                        val (nx, ny) = normalized(startPos.x, startPos.y)
                        receiver.sendTouch("began", nx, ny)
                        receiver.sendTouch("ended", nx, ny)
                        return@awaitEachGesture
                    }
                }

                GestureMode.TOUCH -> {
                    val p = pressed.firstOrNull()
                    if (p == null) {
                        val (nx, ny) = normalized(lastCentroid.x, lastCentroid.y)
                        receiver.sendTouch("ended", nx, ny)
                        return@awaitEachGesture
                    }
                    lastCentroid = p.position
                    val (nx, ny) = normalized(p.position.x, p.position.y)
                    receiver.sendTouch("moved", nx, ny)
                }

                GestureMode.SCROLL -> {
                    if (pressed.isEmpty()) return@awaitEachGesture
                    val centroid = centroidOf(pressed)
                    val dims = getVideoDims()
                    if (dims != null) {
                        val dxNorm = (centroid.x - lastCentroid.x) / size.width
                        val dyNorm = (centroid.y - lastCentroid.y) / size.height
                        receiver.sendScroll(
                            dx = (dxNorm * dims.width).toDouble(),
                            dy = (dyNorm * dims.height).toDouble(),
                        )
                    }
                    lastCentroid = centroid
                }
            }
        }
    }
}

private enum class GestureMode { UNDECIDED, TOUCH, SCROLL }

private fun centroidOf(changes: List<PointerInputChange>): Offset {
    var x = 0f
    var y = 0f
    for (c in changes) {
        x += c.position.x
        y += c.position.y
    }
    return Offset(x / changes.size, y / changes.size)
}
