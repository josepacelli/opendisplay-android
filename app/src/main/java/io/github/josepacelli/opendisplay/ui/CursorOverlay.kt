package io.github.josepacelli.opendisplay.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import io.github.josepacelli.opendisplay.net.CursorImage
import io.github.josepacelli.opendisplay.net.CursorPosition
import io.github.josepacelli.opendisplay.net.PhoneReceiver
import io.github.josepacelli.opendisplay.util.Log
import kotlinx.coroutines.flow.collectLatest

/**
 * The Mac's cursor, drawn locally instead of baked into the video — mirrors
 * the upstream rationale exactly (`Mac/MacSender.swift`'s `localCursor`): a
 * cursor composited into the H.264 stream would carry the full
 * capture->encode->transmit->decode latency, while position updates over the
 * lightweight JSON control channel land in a few ms.
 *
 * Draws in the same coordinate space as [VideoSurface] because both are
 * `fillMaxSize` siblings inside the same aspect-ratio box in
 * `ReceiverScreen` — a plain dot until a `cursorImg` sprite arrives, then the
 * real sprite, anchored at its reported hotspot.
 *
 * @param receiver source of cursor position/sprite updates.
 * @param modifier applied to the drawing surface.
 */
@Composable
fun CursorOverlay(receiver: PhoneReceiver, modifier: Modifier = Modifier) {
    var position by remember { mutableStateOf<CursorPosition?>(null) }
    var sprite by remember { mutableStateOf<DecodedSprite?>(null) }

    LaunchedEffect(receiver) {
        receiver.cursorPosition.collectLatest { position = it }
    }
    LaunchedEffect(receiver) {
        receiver.cursorImage.collectLatest { image -> sprite = decodeSprite(image) }
    }

    val pos = position
    if (pos == null || !pos.visible) return

    val currentSprite = sprite
    if (currentSprite == null) {
        Canvas(modifier = modifier) {
            val center = Offset((pos.x * size.width).toFloat(), (pos.y * size.height).toFloat())
            drawCircle(color = Color.White, radius = 6f, center = center)
            drawCircle(color = Color.Black, radius = 6f, center = center, style = Stroke(width = 1.5f))
        }
    } else {
        CursorSprite(pos = pos, sprite = currentSprite, modifier = modifier)
    }
}

/** Caps how big a peer-supplied cursor PNG can claim to be before it's actually decoded
 * (bounds-only pass first), so a crafted header with huge dimensions can't force an oversized
 * allocation. The real Mac app sends sprites up to ~280x400 (HiDPI drag/resize cursors) — 1024
 * leaves real headroom while still rejecting a decompression-bomb-style claim. */
private const val MAX_SPRITE_DIMENSION_PX = 1024

/** Bounds-checks and decodes [image]'s PNG bytes into a bitmap ready to draw.
 * @param image the peer-supplied cursor sprite to decode.
 * @return the decoded sprite, or `null` if it's oversized or not a valid PNG. */
private fun decodeSprite(image: CursorImage): DecodedSprite? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(image.png, 0, image.png.size, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0 ||
        bounds.outWidth > MAX_SPRITE_DIMENSION_PX || bounds.outHeight > MAX_SPRITE_DIMENSION_PX
    ) {
        Log.warn("cursor sprite rejected: ${bounds.outWidth}x${bounds.outHeight}")
        return null
    }
    val bitmap = try {
        BitmapFactory.decodeByteArray(image.png, 0, image.png.size)?.asImageBitmap()
    } catch (e: Exception) {
        Log.warn("failed to decode cursor sprite", e)
        null
    } ?: return null
    return DecodedSprite(bitmap, image.normalizedWidth, image.normalizedHeight, image.anchorX, image.anchorY)
}

private data class DecodedSprite(
    val bitmap: ImageBitmap,
    val normalizedWidth: Double,
    val normalizedHeight: Double,
    val anchorX: Double,
    val anchorY: Double,
)

/**
 * Custom [Layout] instead of `Image` + `Modifier.offset`: the sprite's pixel
 * size depends on the parent's own measured size (`normalizedWidth`/`Height`
 * are normalized against the Mac's display, per the wire protocol), which
 * isn't known until measurement — a plain offset modifier can't express that.
 *
 * @param pos normalized cursor position within the parent box.
 * @param sprite the decoded sprite to draw.
 * @param modifier applied to the layout.
 */
@Composable
private fun CursorSprite(pos: CursorPosition, sprite: DecodedSprite, modifier: Modifier) {
    Layout(
        content = {
            Image(
                bitmap = sprite.bitmap,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
            )
        },
        modifier = modifier,
    ) { measurables, constraints ->
        val boxWidth = constraints.maxWidth
        val boxHeight = constraints.maxHeight
        val spriteWidthPx = (sprite.normalizedWidth * boxWidth).toInt().coerceAtLeast(1)
        val spriteHeightPx = (sprite.normalizedHeight * boxHeight).toInt().coerceAtLeast(1)
        val placeable = measurables.first().measure(Constraints.fixed(spriteWidthPx, spriteHeightPx))
        layout(boxWidth, boxHeight) {
            val cursorPxX = pos.x * boxWidth
            val cursorPxY = pos.y * boxHeight
            val originX = (cursorPxX - sprite.anchorX * spriteWidthPx).toInt()
            val originY = (cursorPxY - sprite.anchorY * spriteHeightPx).toInt()
            placeable.place(IntOffset(originX, originY))
        }
    }
}
