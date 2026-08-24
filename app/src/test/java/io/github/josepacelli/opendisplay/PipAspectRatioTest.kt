package io.github.josepacelli.opendisplay

import org.junit.Assert.assertEquals
import org.junit.Test

class PipAspectRatioTest {

    @Test
    fun `within range returns the exact dimensions`() {
        assertEquals(1920 to 1080, pipAspectRatio(1920, 1080))
        assertEquals(1200 to 1920, pipAspectRatio(1200, 1920))
    }

    @Test
    fun `zero or negative dimensions fall back to 16 by 9`() {
        assertEquals(16 to 9, pipAspectRatio(0, 1080))
        assertEquals(16 to 9, pipAspectRatio(1920, 0))
        assertEquals(16 to 9, pipAspectRatio(-1, -1))
    }

    @Test
    fun `too wide clamps to 2point39 to 1`() {
        assertEquals(239 to 100, pipAspectRatio(5000, 1000))
    }

    @Test
    fun `too tall clamps to 1 to 2point39`() {
        assertEquals(100 to 239, pipAspectRatio(1000, 5000))
    }

    @Test
    fun `right at the wide edge is not clamped`() {
        assertEquals(2390 to 1000, pipAspectRatio(2390, 1000))
    }

    @Test
    fun `right at the tall edge is not clamped`() {
        assertEquals(1000 to 2390, pipAspectRatio(1000, 2390))
    }
}
