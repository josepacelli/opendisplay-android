package io.github.josepacelli.opendisplay.net

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Exercises the plain data carriers declared alongside [PhoneReceiver] — pure value
 * types, no Android dependency, but otherwise untouched by any test that only drives
 * [PhoneReceiver] itself through its public (Android-dependent) surface. */
class DataTypesTest {

    @Test
    fun `CursorPosition equality, copy and accessors`() {
        val a = CursorPosition(x = 0.25, y = 0.75, visible = true)
        val b = a.copy()
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertEquals(0.25, a.component1(), 0.0)
        assertEquals(0.75, a.component2(), 0.0)
        assertEquals(true, a.component3())
        assertEquals("CursorPosition(x=0.25, y=0.75, visible=true)", a.toString())
    }

    @Test
    fun `CursorImage equality, copy and accessors`() {
        val png = byteArrayOf(1, 2, 3)
        val a = CursorImage(png = png, normalizedWidth = 0.1, normalizedHeight = 0.2, anchorX = 0.3, anchorY = 0.4)
        val b = a.copy()
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertEquals(png, a.component1())
        assertEquals(0.1, a.component2(), 0.0)
        assertEquals(0.2, a.component3(), 0.0)
        assertEquals(0.3, a.component4(), 0.0)
        assertEquals(0.4, a.component5(), 0.0)
    }

    @Test
    fun `VideoFrame equality, copy and accessors`() {
        val sps = byteArrayOf(0x67)
        val pps = byteArrayOf(0x68)
        val slice = byteArrayOf(0x65)
        val a = VideoFrame(sps = sps, pps = pps, vclNalus = listOf(slice), captureMs = 100L, sendMs = 105L)
        val b = a.copy()
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertEquals(sps, a.component1())
        assertEquals(pps, a.component2())
        assertEquals(listOf(slice), a.component3())
        assertEquals(100L, a.component4())
        assertEquals(105L, a.component5())
    }

    @Test
    fun `VideoFrame allows null sps, pps and timestamps`() {
        val a = VideoFrame(sps = null, pps = null, vclNalus = emptyList(), captureMs = null, sendMs = null)
        assertNull(a.sps)
        assertNull(a.pps)
        assertNull(a.captureMs)
        assertNull(a.sendMs)
    }

    @Test
    fun `PerfStats defaults and copy`() {
        val defaults = PerfStats()
        assertEquals(0, defaults.fps)
        assertEquals(0.0, defaults.e2eP50Ms, 0.0)
        assertEquals(0.0, defaults.e2eP95Ms, 0.0)
        assertEquals(0.0, defaults.rttMs, 0.0)

        val a = PerfStats(fps = 60, e2eP50Ms = 14.0, e2eP95Ms = 20.0, rttMs = 33.0)
        val b = a.copy()
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertEquals(60, a.component1())
    }

    @Test
    fun `PeerSignal UpdateMac equality and accessors`() {
        val a = PeerSignal.UpdateMac(message = "Mac update required")
        val b = a.copy()
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertEquals("Mac update required", a.component1())
    }

    @Test
    fun `PeerSignal UpdateAndroid equality and accessors, storeUrl nullable`() {
        val withUrl = PeerSignal.UpdateAndroid(message = "Android update required", storeUrl = "https://github.com")
        val withoutUrl = PeerSignal.UpdateAndroid(message = "Android update required", storeUrl = null)
        assertEquals(withUrl, withUrl.copy())
        assertEquals(withUrl.hashCode(), withUrl.copy().hashCode())
        assertNull(withoutUrl.storeUrl)
        assertEquals("https://github.com", withUrl.component2())
    }

    @Test
    fun `PeerSignal PeerReplaced equality and accessors`() {
        val a = PeerSignal.PeerReplaced(previousAddress = "192.168.1.2", newAddress = "192.168.1.3")
        val b = a.copy()
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertEquals("192.168.1.2", a.component1())
        assertEquals("192.168.1.3", a.component2())
    }

    @Test
    fun `PerfHudPosition has the four screen corners`() {
        assertEquals(
            listOf(
                PerfHudPosition.TOP_START,
                PerfHudPosition.TOP_END,
                PerfHudPosition.BOTTOM_START,
                PerfHudPosition.BOTTOM_END,
            ),
            PerfHudPosition.entries,
        )
        assertEquals(PerfHudPosition.TOP_START, PerfHudPosition.valueOf("TOP_START"))
    }
}
