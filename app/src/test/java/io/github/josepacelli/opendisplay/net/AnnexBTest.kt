package io.github.josepacelli.opendisplay.net

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AnnexBTest {

    private val startCode = byteArrayOf(0, 0, 0, 1)

    @Test
    fun `pure JSON payload is recognized as control`() {
        val payload = """{"type":"hello","pixelsWide":1200}""".toByteArray()
        assertTrue(AnnexB.isControlJson(payload))
    }

    @Test
    fun `JSON control payload containing base64 (no NUL bytes) is still recognized`() {
        // cursorImg-style payload: base64 PNG, never contains a raw 0x00 byte.
        val payload = """{"type":"cursorImg","png":"iVBORw0KGgo="}""".toByteArray()
        assertTrue(AnnexB.isControlJson(payload))
    }

    @Test
    fun `video payload with telemetry prefix is NOT recognized as control despite leading brace`() {
        val telemetry = """{"cap":100,"snd":105}""".toByteArray()
        val slice = startCode + byteArrayOf(0x65, 1, 2, 3) // NALU type 5 (IDR slice)
        val payload = telemetry + slice
        assertFalse(AnnexB.isControlJson(payload))
    }

    @Test
    fun `empty payload is not control`() {
        assertFalse(AnnexB.isControlJson(ByteArray(0)))
    }

    @Test
    fun `parse extracts telemetry prefix, sps, pps and vcl slice from a keyframe payload`() {
        val telemetry = """{"cap":1000,"snd":1010}""".toByteArray()
        val sps = byteArrayOf(0x67, 0x42, 0x00) // type 7
        val pps = byteArrayOf(0x68, 0xCE.toByte()) // type 8
        val slice = byteArrayOf(0x65, 0xAA.toByte(), 0xBB.toByte()) // type 5 (IDR)

        val payload = telemetry + startCode + sps + startCode + pps + startCode + slice

        val parsed = AnnexB.parse(payload)

        assertArrayEquals(telemetry, parsed.telemetryPrefix)
        assertArrayEquals(sps, parsed.sps)
        assertArrayEquals(pps, parsed.pps)
        assertEquals(1, parsed.vclNalus.size)
        assertArrayEquals(slice, parsed.vclNalus[0])
    }

    @Test
    fun `parse skips SEI NALUs`() {
        val sei = byteArrayOf(0x06, 0x01, 0x02) // type 6
        val slice = byteArrayOf(0x41, 0x03) // type 1 (non-IDR slice)
        val payload = startCode + sei + startCode + slice

        val parsed = AnnexB.parse(payload)

        assertNull(parsed.sps)
        assertNull(parsed.pps)
        assertEquals(1, parsed.vclNalus.size)
        assertArrayEquals(slice, parsed.vclNalus[0])
    }

    @Test
    fun `parse handles a payload with a start code but no following slice bytes`() {
        val payload = startCode // just the start code, nothing after it
        val parsed = AnnexB.parse(payload)

        assertNull(parsed.telemetryPrefix)
        assertNull(parsed.sps)
        assertNull(parsed.pps)
        assertTrue(parsed.vclNalus.isEmpty())
    }

    @Test
    fun `parse handles a completely empty payload`() {
        val parsed = AnnexB.parse(ByteArray(0))
        assertNull(parsed.telemetryPrefix)
        assertNull(parsed.sps)
        assertNull(parsed.pps)
        assertTrue(parsed.vclNalus.isEmpty())
    }

    @Test
    fun `parseTelemetry extracts cap and snd fields`() {
        val telemetry = AnnexB.parseTelemetry(""" {"cap":123456789,"snd":123456999} """.toByteArray())
        assertEquals(123456789L, telemetry.captureMs)
        assertEquals(123456999L, telemetry.sendMs)
    }

    @Test
    fun `parseTelemetry returns nulls for a null prefix`() {
        val telemetry = AnnexB.parseTelemetry(null)
        assertNull(telemetry.captureMs)
        assertNull(telemetry.sendMs)
    }
}
