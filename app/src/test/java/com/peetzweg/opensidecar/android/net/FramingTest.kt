package com.peetzweg.opensidecar.android.net

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class FramingTest {

    @Test
    fun `encode prefixes payload with 4-byte big-endian length`() {
        val payload = byteArrayOf(1, 2, 3)
        val framed = Framing.encode(payload)
        assertEquals(7, framed.size)
        assertArrayEquals(byteArrayOf(0, 0, 0, 3), framed.copyOfRange(0, 4))
        assertArrayEquals(payload, framed.copyOfRange(4, 7))
    }

    @Test
    fun `feed returns one frame when a full frame arrives in one call`() {
        val decoder = Framing.FrameDecoder()
        val framed = Framing.encode(byteArrayOf(9, 8, 7))
        val frames = decoder.feed(framed)
        assertEquals(1, frames.size)
        assertArrayEquals(byteArrayOf(9, 8, 7), frames[0])
    }

    @Test
    fun `feed accumulates a frame split across multiple calls`() {
        val decoder = Framing.FrameDecoder()
        val framed = Framing.encode(byteArrayOf(1, 2, 3, 4, 5))

        // First call: only the length header + first 2 payload bytes.
        val part1 = framed.copyOfRange(0, 6)
        val part2 = framed.copyOfRange(6, framed.size)

        assertEquals(0, decoder.feed(part1).size)
        val frames = decoder.feed(part2)
        assertEquals(1, frames.size)
        assertArrayEquals(byteArrayOf(1, 2, 3, 4, 5), frames[0])
    }

    @Test
    fun `feed drains multiple frames delivered in a single batch`() {
        val decoder = Framing.FrameDecoder()
        val batch = Framing.encode(byteArrayOf(1)) + Framing.encode(byteArrayOf(2, 2)) +
            Framing.encode(byteArrayOf(3, 3, 3))
        val frames = decoder.feed(batch)
        assertEquals(3, frames.size)
        assertArrayEquals(byteArrayOf(1), frames[0])
        assertArrayEquals(byteArrayOf(2, 2), frames[1])
        assertArrayEquals(byteArrayOf(3, 3, 3), frames[2])
    }

    @Test
    fun `feed handles an empty payload frame`() {
        val decoder = Framing.FrameDecoder()
        val frames = decoder.feed(Framing.encode(ByteArray(0)))
        assertEquals(1, frames.size)
        assertEquals(0, frames[0].size)
    }

    @Test
    fun `feed rejects an implausibly large length prefix`() {
        val decoder = Framing.FrameDecoder()
        val bogusHeader = ByteArray(4)
        Framing.writeInt32BE(bogusHeader, 0, Framing.MAX_FRAME_SIZE + 1)
        assertThrows(IllegalArgumentException::class.java) {
            decoder.feed(bogusHeader)
        }
    }

    @Test
    fun `feed grows its internal buffer past the initial capacity`() {
        val decoder = Framing.FrameDecoder()
        val bigPayload = ByteArray(200 * 1024) { (it % 251).toByte() }
        val frames = decoder.feed(Framing.encode(bigPayload))
        assertEquals(1, frames.size)
        assertArrayEquals(bigPayload, frames[0])
    }
}
