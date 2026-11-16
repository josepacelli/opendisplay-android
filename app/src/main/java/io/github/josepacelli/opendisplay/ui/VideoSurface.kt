package io.github.josepacelli.opendisplay.ui

import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.viewinterop.AndroidView
import io.github.josepacelli.opendisplay.net.PhoneReceiver
import io.github.josepacelli.opendisplay.util.Log
import io.github.josepacelli.opendisplay.video.VideoDecoder
import kotlin.math.abs
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive

private const val TOUCH_SLOP_PX = 24f

/** Perpendicular-to-the-screen altitude (PROTOCOL.md §6.1: "altitude pi/2 = perpendicular") —
 * the default reported for a stylus `down` event, whose real tilt isn't available yet (see
 * [handleStylusGesture]). */
private const val PERPENDICULAR_ALTITUDE = Math.PI / 2

/** Distance change (px) a two-finger gesture needs before it's recognized as a pinch
 * rather than a two-finger scroll — mirrors [TOUCH_SLOP_PX]'s role for one-finger drags. */
private const val PINCH_SLOP_PX = 24f

private const val MIN_ZOOM = 1f
private const val MAX_ZOOM = 5f

/** Desyncs a single decoder instance must hit before the "unstable connection" banner shows —
 * an isolated blip recovers on its own via the keyframe request alone and isn't worth alarming
 * the user over; only sustained trouble is. */
private const val UNSTABLE_BANNER_THRESHOLD = 3

/** Decoded frame size, once MediaCodec reports its real output format. */
data class VideoDims(val width: Int, val height: Int)

/**
 * Hosts the `SurfaceView` MediaCodec renders into and turns touch on it into
 * `touch`/`scroll` wire messages. Sized by the caller — [ReceiverScreen] wraps
 * this in an aspect-ratio [androidx.compose.foundation.layout.Box] (video
 * width/height first guessed from this device's own announced panel size,
 * then refined via [onVideoDimsChanged]) so it, and the cursor overlay next
 * to it, both fill that same letterboxed/pillarboxed rect.
 *
 * @param receiver source of decoded video frames and touch/scroll sink.
 * @param videoDims current known decoded frame size, or `null` before the first one arrives.
 * @param onVideoDimsChanged called when the decoder reports a real (possibly new) output size.
 * @param zoomEnabled whether a two-finger spread should pinch-zoom the video locally; when off,
 * two fingers always scroll (and any active zoom is reset to 100%).
 * @param oneFingerScroll whether a one-finger drag scrolls instead of dragging the mouse (a tap
 * still clicks).
 * @param modifier applied to the underlying `SurfaceView`.
 */
@Composable
fun VideoSurface(
    receiver: PhoneReceiver,
    videoDims: VideoDims?,
    onVideoDimsChanged: (VideoDims) -> Unit,
    zoomEnabled: Boolean = true,
    oneFingerScroll: Boolean = false,
    modifier: Modifier = Modifier,
) {
    var decoder by remember { mutableStateOf<VideoDecoder?>(null) }
    val currentReceiver by rememberUpdatedState(receiver)
    val currentDims by rememberUpdatedState(videoDims)
    val onDimsChanged by rememberUpdatedState(onVideoDimsChanged)
    val zoomScale = remember { mutableFloatStateOf(1f) }
    val zoomPan = remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(zoomEnabled) {
        if (!zoomEnabled) {
            zoomScale.floatValue = 1f
            zoomPan.value = Offset.Zero
        }
    }

    LaunchedEffect(receiver) {
        receiver.videoFrames.collect { frame -> decoder?.submit(frame) }
    }

    AndroidView(
        modifier = modifier
            .pointerInput(receiver, zoomEnabled, oneFingerScroll) {
                while (currentCoroutineContext().isActive) {
                    try {
                        awaitEachGesture {
                            handleGesture(
                                getVideoDims = { currentDims },
                                receiver = currentReceiver,
                                zoomScale = zoomScale,
                                zoomPan = zoomPan,
                                zoomEnabled = zoomEnabled,
                                oneFingerScroll = oneFingerScroll,
                            )
                        }
                    } catch (c: CancellationException) {
                        if (!currentCoroutineContext().isActive) throw c
                    }
                }
            }
            .graphicsLayer {
                scaleX = zoomScale.floatValue
                scaleY = zoomScale.floatValue
                transformOrigin = TransformOrigin(0f, 0f)
                translationX = zoomPan.value.x
                translationY = zoomPan.value.y
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
                            onError = { desyncCount ->
                                currentReceiver.requestKeyframe()
                                if (desyncCount > UNSTABLE_BANNER_THRESHOLD) {
                                    currentReceiver.notifyConnectionUnstable()
                                }
                            },
                        )
                        currentReceiver.requestKeyframe()
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
 * One full gesture on the video surface. Single-finger drag -> `touch`
 * (began/moved/ended). A second finger joining before the first has moved
 * past a small slop switches the whole gesture to a two-finger mode instead
 * — this avoids ever sending `began` for what turns out to be a two-finger
 * gesture, which would otherwise leave the Mac's mouse button stuck down
 * (see `Mac/InputInjector.swift`: `began` maps straight to `mouseDown`).
 * The two-finger gesture itself stays undecided between `scroll` (centroid
 * delta of every active pointer, sent to the Mac) and a local pinch-zoom of
 * the video (finger-spread delta, never sent to the Mac) until one of them
 * clears its own slop — spread wins ties, since it's checked first.
 * With [zoomEnabled] off the spread branch never fires, so two fingers
 * always scroll.
 *
 * Three fingers pan the zoomed view instead (like grabbing the picture):
 * while zoomed in, dragging them moves which part of the video is visible,
 * clamped so the video always covers the whole surface. Also gated by
 * [zoomEnabled]; at 100% zoom there is nothing to pan, so the gesture is a
 * no-op. Never sent to the Mac.
 *
 * While zoomed, single-finger touch positions are mapped back through the
 * current zoom/pan before being normalized, so touch injection keeps landing
 * on the same Mac-screen point the finger is visually over.
 *
 * Runs as one gesture inside [androidx.compose.foundation.gestures.awaitEachGesture],
 * which drains any leftover fingers before arming the next gesture.
 *
 * @param getVideoDims current decoded video size, needed to convert scroll deltas to video pixels.
 * @param receiver where resulting `touch`/`scroll` messages are sent.
 * @param zoomScale current pinch-zoom scale (1f = fit, no zoom); mutated as pinches happen.
 * @param zoomPan current pinch-zoom pan offset in screen px; mutated as pinches happen.
 * @param zoomEnabled whether a two-finger spread is allowed to commit to a local pinch-zoom.
 * @param oneFingerScroll when on, a single finger that moves past the slop commits to `scroll`
 * (not `touch` began/moved), so dragging never selects or drags; a tap still clicks.
 */
private suspend fun AwaitPointerEventScope.handleGesture(
    getVideoDims: () -> VideoDims?,
    receiver: PhoneReceiver,
    zoomScale: MutableState<Float>,
    zoomPan: MutableState<Offset>,
    zoomEnabled: Boolean,
    oneFingerScroll: Boolean,
) {
    val first = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Main)
    if (first.type == PointerType.Stylus && receiver.peerSupportsPencil) {
        handleStylusGesture(first, receiver, zoomScale.value, zoomPan.value)
        return
    }
    val startPos = first.position
    var committedMode: GestureMode = GestureMode.UNDECIDED
    var lastCentroid = startPos
    var scrollFingerCount = 0
    var twoFingerStartCentroid = Offset.Zero
    var twoFingerStartDistance = 0f
    var zoomStartScale = 1f
    var zoomStartPan = Offset.Zero
    var zoomStartDistance = 0f
    var zoomStartCentroid = Offset.Zero
    var panStartPan = Offset.Zero
    var panStartCentroid = Offset.Zero

    fun normalized(x: Float, y: Float): Pair<Double, Double> {
        val contentX = (x - zoomPan.value.x) / zoomScale.value
        val contentY = (y - zoomPan.value.y) / zoomScale.value
        return (contentX / size.width).toDouble().coerceIn(0.0, 1.0) to
            (contentY / size.height).toDouble().coerceIn(0.0, 1.0)
    }

    while (true) {
        val event = awaitPointerEvent()
        val pressed = event.changes.filter { it.pressed }

        when (committedMode) {
            GestureMode.UNDECIDED -> when {
                pressed.size >= 3 && zoomEnabled -> {
                    committedMode = GestureMode.PAN
                    panStartPan = zoomPan.value
                    panStartCentroid = centroidOf(pressed)
                }

                pressed.size >= 2 -> {
                    committedMode = GestureMode.TWO_FINGER_UNDECIDED
                    twoFingerStartCentroid = centroidOf(pressed)
                    twoFingerStartDistance = distanceOf(pressed)
                }

                pressed.size == 1 -> {
                    val moved = pressed[0].position - startPos
                    if (abs(moved.x) > TOUCH_SLOP_PX || abs(moved.y) > TOUCH_SLOP_PX) {
                        if (oneFingerScroll) {
                            // Nothing was sent yet, so no mouse button can get stuck.
                            committedMode = GestureMode.SCROLL
                        } else {
                            committedMode = GestureMode.TOUCH
                            val (nx, ny) = normalized(first.position.x, first.position.y)
                            receiver.sendTouch("began", nx, ny)
                            val (mx, my) = normalized(pressed[0].position.x, pressed[0].position.y)
                            receiver.sendTouch("moved", mx, my)
                            lastCentroid = pressed[0].position
                        }
                    }
                }

                else -> {
                    val (nx, ny) = normalized(startPos.x, startPos.y)
                    receiver.sendTouch("began", nx, ny)
                    receiver.sendTouch("ended", nx, ny)
                    return
                }
            }

            GestureMode.TWO_FINGER_UNDECIDED -> {
                if (pressed.size < 2) return
                val centroid = centroidOf(pressed)
                val distance = distanceOf(pressed)
                when {
                    zoomEnabled && pressed.size >= 3 -> {
                        committedMode = GestureMode.PAN
                        panStartPan = zoomPan.value
                        panStartCentroid = centroid
                    }

                    zoomEnabled && abs(distance - twoFingerStartDistance) > PINCH_SLOP_PX -> {
                        committedMode = GestureMode.ZOOM
                        zoomStartScale = zoomScale.value
                        zoomStartPan = zoomPan.value
                        zoomStartDistance = distance
                        zoomStartCentroid = centroid
                    }

                    (centroid - twoFingerStartCentroid).getDistance() > TOUCH_SLOP_PX -> {
                        committedMode = GestureMode.SCROLL
                    }
                }
            }

            GestureMode.TOUCH -> {
                val p = pressed.firstOrNull()
                if (p == null) {
                    val (nx, ny) = normalized(lastCentroid.x, lastCentroid.y)
                    receiver.sendTouch("ended", nx, ny)
                    return
                }
                lastCentroid = p.position
                val (nx, ny) = normalized(p.position.x, p.position.y)
                receiver.sendTouch("moved", nx, ny)
            }

            GestureMode.SCROLL -> {
                if (pressed.isEmpty()) return
                val centroid = centroidOf(pressed)
                // Entering SCROLL, or a finger added or lifted, moves the centroid without any
                // real motion; re-anchor instead of sending that jump as a scroll.
                if (pressed.size != scrollFingerCount) {
                    scrollFingerCount = pressed.size
                } else {
                    val dims = getVideoDims()
                    if (dims != null) {
                        val dxNorm = (centroid.x - lastCentroid.x) / size.width
                        val dyNorm = (centroid.y - lastCentroid.y) / size.height
                        receiver.sendScroll(
                            dx = (dxNorm * dims.width).toDouble(),
                            dy = (dyNorm * dims.height).toDouble(),
                        )
                    }
                }
                lastCentroid = centroid
            }

            GestureMode.ZOOM -> {
                if (pressed.size < 2) return
                val centroid = centroidOf(pressed)
                val distance = distanceOf(pressed)
                val newScale = (zoomStartScale * (distance / zoomStartDistance))
                    .coerceIn(MIN_ZOOM, MAX_ZOOM)
                val anchorContent = (zoomStartCentroid - zoomStartPan) / zoomStartScale
                val newPan = centroid - anchorContent * newScale
                val maxPanX = 0f
                val minPanX = size.width - size.width * newScale
                val maxPanY = 0f
                val minPanY = size.height - size.height * newScale
                zoomScale.value = newScale
                zoomPan.value = Offset(
                    newPan.x.coerceIn(minPanX, maxPanX),
                    newPan.y.coerceIn(minPanY, maxPanY),
                )
            }

            GestureMode.PAN -> {
                if (pressed.size < 3) return
                val centroid = centroidOf(pressed)
                val newPan = panStartPan + (centroid - panStartCentroid)
                val maxPanX = 0f
                val minPanX = size.width - size.width * zoomScale.value
                val maxPanY = 0f
                val minPanY = size.height - size.height * zoomScale.value
                zoomPan.value = Offset(
                    newPan.x.coerceIn(minPanX, maxPanX),
                    newPan.y.coerceIn(minPanY, maxPanY),
                )
            }
        }
    }
}

/**
 * One stylus contact -> `pencil` down/move/up (PROTOCOL.md §6.1, pv 3). Deliberately its own
 * single-pointer loop instead of a branch inside [handleGesture]'s multi-touch state machine:
 * a stylus stroke has none of that machinery's concerns (no pinch/pan/scroll, no ambiguity to
 * resolve between modes), so folding it in there would only add a rarely-exercised branch to
 * already-dense code. Any other pointer that joins mid-stroke is ignored — palm-rejection
 * false positives aside, drawing with a second finger down isn't a real use case here.
 *
 * Tilt (azimuth/altitude) isn't on [PointerInputChange] — only the raw [MotionEvent] carries
 * it, and [awaitFirstDown] doesn't hand that back, so the `down` phase reports
 * [PERPENDICULAR_ALTITUDE]/`0` (harmless: the real tilt lands within the first `move`, a few
 * milliseconds later for any real stroke). Every event after that comes from
 * [AwaitPointerEventScope.awaitPointerEvent] directly, whose [PointerEvent.motionEvent] does
 * carry it.
 *
 * @param first the stylus's first-down change.
 * @param receiver where resulting `pencil` messages are sent.
 * @param zoomScale current pinch-zoom scale, same convention as [handleGesture]'s `normalized`.
 * @param zoomPan current pinch-zoom pan offset, same convention as [handleGesture]'s `normalized`.
 */
private suspend fun AwaitPointerEventScope.handleStylusGesture(
    first: PointerInputChange,
    receiver: PhoneReceiver,
    zoomScale: Float,
    zoomPan: Offset,
) {
    fun normalized(x: Float, y: Float): Pair<Double, Double> {
        val contentX = (x - zoomPan.x) / zoomScale
        val contentY = (y - zoomPan.y) / zoomScale
        return (contentX / size.width).toDouble().coerceIn(0.0, 1.0) to
            (contentY / size.height).toDouble().coerceIn(0.0, 1.0)
    }

    val (dx, dy) = normalized(first.position.x, first.position.y)
    receiver.sendPencil("down", dx, dy, first.pressure.toDouble(), azimuth = 0.0, altitude = PERPENDICULAR_ALTITUDE)

    var loggedFirstMove = false
    while (true) {
        val event = awaitPointerEvent()
        val change = event.changes.firstOrNull { it.id == first.id } ?: return
        val (nx, ny) = normalized(change.position.x, change.position.y)
        val (azimuth, altitude) = tiltOf(event.motionEvent)
        if (!change.pressed) {
            receiver.sendPencil("up", nx, ny, 0.0, azimuth, altitude)
            return
        }
        if (!loggedFirstMove) {
            loggedFirstMove = true
            Log.info("pencil move (pressure=${change.pressure}, azimuth=$azimuth, altitude=$altitude)")
        }
        receiver.sendPencil("move", nx, ny, change.pressure.toDouble(), azimuth, altitude)
    }
}

/** Reads stylus tilt off the raw [MotionEvent] Compose's [PointerInputChange] doesn't expose.
 * Always reads pointer index 0 — Compose's [PointerInputChange.id] is a Compose-internal
 * sequence number, not the native `MotionEvent` pointer id, so there's no reliable way to
 * correlate a specific [PointerInputChange] against a specific index in a batched native
 * event; index 0 is correct for the actual case this runs in ([handleStylusGesture] only
 * tracks one pointer at a time; a second one being simultaneously down is a co-touch corner
 * case that already falls outside this function's precision contract).
 * @param motionEvent the event backing this [PointerEvent], or `null` for a synthetic one.
 * @return (azimuth, altitude) in radians, or `(0.0, PERPENDICULAR_ALTITUDE)` if unavailable. */
private fun tiltOf(motionEvent: MotionEvent?): Pair<Double, Double> {
    val event = motionEvent ?: return 0.0 to PERPENDICULAR_ALTITUDE
    if (event.pointerCount == 0) return 0.0 to PERPENDICULAR_ALTITUDE
    val tilt = event.getAxisValue(MotionEvent.AXIS_TILT, 0)
    return event.getOrientation(0).toDouble() to altitudeFromAndroidTilt(tilt)
}

/** Android's `AXIS_TILT` is the angle FROM perpendicular (0 = pen straight up); the wire
 * protocol's altitude (PROTOCOL.md §6.1) is the angle FROM the screen plane (pi/2 = straight
 * up) — same physical quantity, complementary reference, hence the subtraction. Split out
 * from [tiltOf] so the actual conversion is testable without a real `MotionEvent` (see
 * `VideoSurfaceTiltTest`) — confirmed against a real S Pen: tilting the pen away from
 * perpendicular lowered this value below [PERPENDICULAR_ALTITUDE], as expected.
 * @param androidTilt `MotionEvent.AXIS_TILT`'s reading, radians.
 * @return the wire protocol's altitude, radians. */
internal fun altitudeFromAndroidTilt(androidTilt: Float): Double = PERPENDICULAR_ALTITUDE - androidTilt.toDouble()

/** Which wire message (or local effect, for [GestureMode.ZOOM]/[GestureMode.PAN]) a gesture in
 * progress will become, once enough pointers/movement make that clear — see [handleGesture]. */
private enum class GestureMode { UNDECIDED, TWO_FINGER_UNDECIDED, TOUCH, SCROLL, ZOOM, PAN }

/** Average position of every active pointer, for multi-finger scroll.
 * @param changes the currently active pointers.
 * @return their centroid, in local coordinates. */
private fun centroidOf(changes: List<PointerInputChange>): Offset {
    var x = 0f
    var y = 0f
    for (c in changes) {
        x += c.position.x
        y += c.position.y
    }
    return Offset(x / changes.size, y / changes.size)
}

/** Spread between the first two active pointers, for pinch-zoom recognition.
 * @param changes the currently active pointers.
 * @return their distance apart, in local coordinates, or 0f if fewer than two are active. */
private fun distanceOf(changes: List<PointerInputChange>): Float =
    if (changes.size < 2) 0f else (changes[0].position - changes[1].position).getDistance()
