package io.github.josepacelli.opendisplay.video

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class H264SpsTest {

    // Baseline profile, 1280x720, no cropping (dims exactly divisible by 16).
    // Bits built independently of H264Sps with a standalone Exp-Golomb writer.
    private val baseline1280x720 = byteArrayOf(
        0x67.toByte(), 0x42, 0x00, 0x1E, 0xF4.toByte(), 0x02, 0x80.toByte(), 0x2D, 0xC8.toByte(),
    )

    // High profile (chroma/scaling-matrix fields present), 4:2:0, 1366x1024 —
    // the exact Mac Mirror-mode resolution from issue #44, needs horizontal cropping
    // since 1366 isn't a multiple of 16 (padded coded width is 1376).
    private val high1366x1024 = byteArrayOf(
        0x67.toByte(), 0x64, 0x00, 0x1F, 0xAC.toByte(), 0xE8.toByte(), 0x05, 0x60, 0x20, 0x79, 0xB4.toByte(),
    )

    @Test
    fun `parses baseline profile SPS with no cropping`() {
        val dims = H264Sps.parseDimensions(baseline1280x720)
        assertEquals(H264Sps.Dimensions(1280, 720), dims)
    }

    @Test
    fun `parses high profile SPS with chroma info and horizontal cropping`() {
        val dims = H264Sps.parseDimensions(high1366x1024)
        assertEquals(H264Sps.Dimensions(1366, 1024), dims)
    }

    @Test
    fun `returns null for a truncated SPS`() {
        val dims = H264Sps.parseDimensions(byteArrayOf(0x67, 0x42))
        assertNull(dims)
    }

    @Test
    fun `returns null for an empty SPS`() {
        assertNull(H264Sps.parseDimensions(ByteArray(0)))
    }
}
