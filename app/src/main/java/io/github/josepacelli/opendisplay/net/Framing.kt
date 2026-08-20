// Derived from the drainFrames cursor-based drain in iOS/PhoneReceiver.swift, peetzweg/opendisplay.
// Copyright (c) 2026 Philip Poloczek. Licensed under GPL-3.0.

package io.github.josepacelli.opendisplay.net

/**
 * Wire framing shared by both directions of the OpenDisplay socket:
 * `[4-byte big-endian length][payload]`. See .claude/skills/wire-protocol/SKILL.md.
 *
 * [Framing] has no Android/socket dependency on purpose — it is pure byte
 * manipulation so it can be unit-tested on the plain JVM.
 */
object Framing {

    private const val HEADER_SIZE = 4

    /** Defensive cap against a corrupt/hostile length prefix causing a huge
     * allocation. The wire protocol itself does not define a max frame size;
     * this is comfortably above the largest keyframe this app should ever see. */
    const val MAX_FRAME_SIZE = 16 * 1024 * 1024

    /** Wraps [payload] with its 4-byte big-endian length prefix.
     * @param payload the message body to frame.
     * @return a new array: `[4-byte length][payload]`. */
    fun encode(payload: ByteArray): ByteArray {
        val out = ByteArray(HEADER_SIZE + payload.size)
        writeInt32BE(out, 0, payload.size)
        System.arraycopy(payload, 0, out, HEADER_SIZE, payload.size)
        return out
    }

    /** Reads a 4-byte big-endian integer.
     * @param buffer the array to read from.
     * @param offset index of the first (most significant) byte.
     * @return the decoded integer. */
    fun readInt32BE(buffer: ByteArray, offset: Int): Int {
        return ((buffer[offset].toInt() and 0xFF) shl 24) or
            ((buffer[offset + 1].toInt() and 0xFF) shl 16) or
            ((buffer[offset + 2].toInt() and 0xFF) shl 8) or
            (buffer[offset + 3].toInt() and 0xFF)
    }

    /** Writes a 4-byte big-endian integer.
     * @param buffer the array to write into.
     * @param offset index of the first (most significant) byte to write.
     * @param value the integer to encode. */
    fun writeInt32BE(buffer: ByteArray, offset: Int, value: Int) {
        buffer[offset] = (value ushr 24).toByte()
        buffer[offset + 1] = (value ushr 16).toByte()
        buffer[offset + 2] = (value ushr 8).toByte()
        buffer[offset + 3] = value.toByte()
    }

    /**
     * Accumulates fed bytes and extracts complete `[len][payload]` frames,
     * mirroring the cursor-based drain in the upstream `PhoneReceiver.swift`
     * (`drainFrames`): compact the internal buffer once per batch instead of
     * on every single frame.
     *
     * Not thread-safe — feed from a single reader thread/coroutine.
     */
    class FrameDecoder {
        private var buffer = ByteArray(64 * 1024)
        private var size = 0

        /** Feeds [length] bytes from [data] (defaults to the whole array) and
         * returns every complete frame payload now available, in order.
         * @param data source bytes just read off the wire.
         * @param length how many bytes of [data] are valid, from index 0.
         * @return every complete frame payload decoded from the accumulated buffer so far.
         * @throws IllegalArgumentException if a frame's declared length is negative or exceeds [MAX_FRAME_SIZE].
         */
        fun feed(data: ByteArray, length: Int = data.size): List<ByteArray> {
            ensureCapacity(size + length)
            System.arraycopy(data, 0, buffer, size, length)
            size += length

            val frames = mutableListOf<ByteArray>()
            var cursor = 0
            while (size - cursor >= HEADER_SIZE) {
                val len = readInt32BE(buffer, cursor)
                require(len in 0..MAX_FRAME_SIZE) { "invalid frame length $len" }
                if (size - cursor < HEADER_SIZE + len) break
                val start = cursor + HEADER_SIZE
                frames.add(buffer.copyOfRange(start, start + len))
                cursor = start + len
            }
            if (cursor > 0) {
                System.arraycopy(buffer, cursor, buffer, 0, size - cursor)
                size -= cursor
            }
            return frames
        }

        /** Grows [buffer] (doubling) until it can hold at least [needed] bytes.
         * @param needed minimum required buffer size, in bytes. */
        private fun ensureCapacity(needed: Int) {
            if (needed <= buffer.size) return
            var newSize = buffer.size * 2
            while (newSize < needed) newSize *= 2
            buffer = buffer.copyOf(newSize)
        }
    }
}
