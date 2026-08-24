package io.github.josepacelli.opendisplay

/**
 * Picture-in-picture aspect ratio, clamped to what Android's PiP window
 * accepts (between 1:2.39 and 2.39:1). Returned as a plain numerator/
 * denominator pair rather than [android.util.Rational] so this stays
 * unit-testable without the Android framework.
 *
 * @param width session panel width in pixels, or non-positive if unknown.
 * @param height session panel height in pixels, or non-positive if unknown.
 * @return numerator/denominator pair, clamped to the PiP-accepted range.
 */
fun pipAspectRatio(width: Int, height: Int): Pair<Int, Int> {
    if (width <= 0 || height <= 0) return 16 to 9
    val aspect = width.toFloat() / height
    return when {
        aspect > 2.39f -> 239 to 100
        aspect < 1f / 2.39f -> 100 to 239
        else -> width to height
    }
}
