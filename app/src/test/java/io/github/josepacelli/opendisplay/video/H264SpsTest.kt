package io.github.josepacelli.opendisplay.video

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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

    @Test
    fun `Dimensions data class generated members are covered`() {
        val a = H264Sps.Dimensions(10, 20)
        val b = a.copy()
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertEquals(10, a.component1())
        assertEquals(20, a.component2())
        assertEquals("Dimensions(width=10, height=20)", a.toString())
    }

    // --- Synthetic bitstreams exercising every branch of parseDimensions ---
    //
    // A real encoder's SPS never touches most of this parser's optional syntax
    // (4:4:4/4:2:2/monochrome chroma, scaling matrices, field pictures, POC type 1,
    // emulation-prevention bytes) — the two samples above only cover the common
    // path. These streams are hand-assembled bit-by-bit with SpsBitWriter to reach
    // every remaining branch through the same public parseDimensions() entry point
    // the real samples use, rather than exposing internals just for testing.

    @Test
    fun `4-4-4 chroma (separate colour plane), progressive, no scaling matrix or cropping`() {
        val sps = SpsBitWriter().apply {
            u8(244) // profileIdc: High 4:4:4, in the chroma-info profile set
            u8(0) // constraint flags
            u8(0) // levelIdc
            ue(0) // seqParameterSetId
            ue(3) // chromaFormatIdc = 3 (4:4:4)
            u(0, 1) // separateColourPlaneFlag
            ue(0) // bitDepthLumaMinus8
            ue(0) // bitDepthChromaMinus8
            u(0, 1) // qpprimeYZeroTransformBypassFlag
            u(0, 1) // seqScalingMatrixPresentFlag = 0
            ue(0) // log2MaxFrameNumMinus4
            ue(0) // picOrderCntType = 0
            ue(0) // (picOrderCntType 0's log2MaxPicOrderCntLsbMinus4)
            ue(0) // maxNumRefFrames
            u(0, 1) // gapsInFrameNumValueAllowedFlag
            ue(4) // picWidthInMbsMinus1 -> width 80
            ue(2) // picHeightInMapUnitsMinus1
            u(1, 1) // frameMbsOnlyFlag = 1 (progressive)
            u(0, 1) // direct8x8InferenceFlag
            u(0, 1) // frame_cropping_flag = 0
        }.toSpsNalu(headerByte = 0x67)

        assertEquals(H264Sps.Dimensions(80, 48), H264Sps.parseDimensions(sps))
    }

    @Test
    fun `POC type 1 and field pictures (frameMbsOnlyFlag=0), default 4-2-0 chroma`() {
        val sps = SpsBitWriter().apply {
            u8(66) // profileIdc: Baseline, NOT in the chroma-info profile set
            u8(0)
            u8(0)
            ue(0) // seqParameterSetId
            // chroma-info block skipped entirely for this profile — chromaFormatIdc stays 1
            ue(0) // log2MaxFrameNumMinus4
            ue(1) // picOrderCntType = 1
            u(0, 1) // deltaPicOrderAlwaysZeroFlag
            se(0) // offsetForNonRefPic
            se(0) // offsetForTopToBottomField
            ue(0) // numRefFramesInPicOrderCntCycle (repeat 0 times)
            ue(0) // maxNumRefFrames
            u(0, 1) // gapsInFrameNumValueAllowedFlag
            ue(4) // picWidthInMbsMinus1 -> width 80
            ue(2) // picHeightInMapUnitsMinus1
            u(0, 1) // frameMbsOnlyFlag = 0 (field pictures)
            u(0, 1) // mbAdaptiveFrameFieldFlag
            u(0, 1) // direct8x8InferenceFlag
            u(0, 1) // frame_cropping_flag = 0
        }.toSpsNalu(headerByte = 0x67)

        assertEquals(H264Sps.Dimensions(80, 96), H264Sps.parseDimensions(sps))
    }

    @Test
    fun `monochrome chroma (0) with a scaling matrix present`() {
        val sps = SpsBitWriter().apply {
            u8(100) // profileIdc: High, in the chroma-info profile set
            u8(0)
            u8(0)
            ue(0) // seqParameterSetId
            ue(0) // chromaFormatIdc = 0 (monochrome)
            ue(0) // bitDepthLumaMinus8
            ue(0) // bitDepthChromaMinus8
            u(0, 1) // qpprimeYZeroTransformBypassFlag
            u(1, 1) // seqScalingMatrixPresentFlag = 1
            // 8 lists (chromaFormatIdc != 3): only list 0 (size 16) and list 6 (size 64)
            // present, each with a single delta_scale that immediately zeroes nextScale
            // so the rest of that list's loop skips its se() reads.
            u(1, 1) // list 0 present -> skipScalingList(size = 16)
            se(-8) // delta_scale: lastScale(8) + (-8) = 0 -> nextScale stays 0 from here
            u(0, 1) // list 1 absent
            u(0, 1) // list 2 absent
            u(0, 1) // list 3 absent
            u(0, 1) // list 4 absent
            u(0, 1) // list 5 absent
            u(1, 1) // list 6 present -> skipScalingList(size = 64)
            se(-8) // same zeroing trick
            u(0, 1) // list 7 absent
            ue(0) // log2MaxFrameNumMinus4
            ue(0) // picOrderCntType = 0
            ue(0)
            ue(0) // maxNumRefFrames
            u(0, 1) // gapsInFrameNumValueAllowedFlag
            ue(4) // picWidthInMbsMinus1 -> width 80
            ue(2) // picHeightInMapUnitsMinus1
            u(1, 1) // frameMbsOnlyFlag = 1
            u(0, 1) // direct8x8InferenceFlag
            u(0, 1) // frame_cropping_flag = 0
        }.toSpsNalu(headerByte = 0x67)

        assertEquals(H264Sps.Dimensions(80, 48), H264Sps.parseDimensions(sps))
    }

    @Test
    fun `4-2-2 chroma (2)`() {
        val sps = SpsBitWriter().apply {
            u8(100) // profileIdc: High, in the chroma-info profile set
            u8(0)
            u8(0)
            ue(0) // seqParameterSetId
            ue(2) // chromaFormatIdc = 2 (4:2:2)
            ue(0) // bitDepthLumaMinus8
            ue(0) // bitDepthChromaMinus8
            u(0, 1) // qpprimeYZeroTransformBypassFlag
            u(0, 1) // seqScalingMatrixPresentFlag = 0
            ue(0) // log2MaxFrameNumMinus4
            ue(0) // picOrderCntType = 0
            ue(0)
            ue(0) // maxNumRefFrames
            u(0, 1) // gapsInFrameNumValueAllowedFlag
            ue(4) // picWidthInMbsMinus1 -> width 80
            ue(2) // picHeightInMapUnitsMinus1
            u(1, 1) // frameMbsOnlyFlag = 1
            u(0, 1) // direct8x8InferenceFlag
            u(0, 1) // frame_cropping_flag = 0
        }.toSpsNalu(headerByte = 0x67)

        assertEquals(H264Sps.Dimensions(80, 48), H264Sps.parseDimensions(sps))
    }

    @Test
    fun `cropping that consumes the whole width returns null instead of a non-positive size`() {
        val sps = SpsBitWriter().apply {
            u8(66) // profileIdc: Baseline, chromaFormatIdc stays default 1 (subWidthC = 2)
            u8(0)
            u8(0)
            ue(0) // seqParameterSetId
            ue(0) // log2MaxFrameNumMinus4
            ue(0) // picOrderCntType = 0
            ue(0)
            ue(0) // maxNumRefFrames
            u(0, 1) // gapsInFrameNumValueAllowedFlag
            ue(0) // picWidthInMbsMinus1 -> raw width 16
            ue(2) // picHeightInMapUnitsMinus1
            u(1, 1) // frameMbsOnlyFlag = 1
            u(0, 1) // direct8x8InferenceFlag
            u(1, 1) // frame_cropping_flag = 1
            ue(4) // cropLeft
            ue(4) // cropRight -> cropUnitX(2) * (4+4) = 16 == raw width -> width <= 0
            ue(0) // cropTop
            ue(0) // cropBottom
        }.toSpsNalu(headerByte = 0x67)

        assertNull(H264Sps.parseDimensions(sps))
    }

    @Test
    fun `emulation-prevention byte is stripped before parsing`() {
        // profileIdc=0x00, constraint flags=0x00 line up two zero bytes right after the
        // NAL header, so inserting an escape 0x03 there round-trips exactly like the
        // un-escaped stream once H264Sps strips it back out.
        val unescaped = SpsBitWriter().apply {
            u8(0) // profileIdc = 0 (not in the chroma-info profile set)
            u8(0) // constraint flags
            u8(5) // levelIdc (unused by the parser)
            ue(0) // seqParameterSetId
            ue(0) // log2MaxFrameNumMinus4
            ue(0) // picOrderCntType = 0
            ue(0)
            ue(0) // maxNumRefFrames
            u(0, 1) // gapsInFrameNumValueAllowedFlag
            ue(4) // picWidthInMbsMinus1 -> width 80
            ue(2) // picHeightInMapUnitsMinus1
            u(1, 1) // frameMbsOnlyFlag = 1
            u(0, 1) // direct8x8InferenceFlag
            u(0, 1) // frame_cropping_flag = 0
        }.toBytes()

        val escaped = byteArrayOf(0x67, unescaped[0], unescaped[1], 0x03) + unescaped.copyOfRange(2, unescaped.size)

        assertEquals(H264Sps.Dimensions(80, 48), H264Sps.parseDimensions(escaped))
    }

    @Test
    fun `more than 32 leading zero bits is a malformed exp-golomb code, returns null`() {
        val sps = byteArrayOf(0x67, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
        assertNull(H264Sps.parseDimensions(sps))
    }

    @Test
    fun `running out of bits mid-parse returns null`() {
        val sps = byteArrayOf(0x67, 0x00, 0x00, 0x00) // exactly the 3 header fields, nothing more
        assertNull(H264Sps.parseDimensions(sps))
    }

    /** Bit-exact Exp-Golomb/fixed-width writer, MSB-first per byte — the inverse of
     * [H264Sps]'s private `BitReader`. Test-only: lets the synthetic-bitstream tests
     * above drive [H264Sps.parseDimensions] (its only public entry point) into every
     * branch of the real Exp-Golomb parser, instead of exposing internals. */
    private class SpsBitWriter {
        private val bits = mutableListOf<Int>()

        fun u(value: Int, n: Int) {
            for (i in n - 1 downTo 0) bits.add((value shr i) and 1)
        }

        fun u8(value: Int) = u(value, 8)

        fun ue(value: Int) {
            val codeNum = value + 1
            val bitLength = 32 - Integer.numberOfLeadingZeros(codeNum)
            repeat(bitLength - 1) { bits.add(0) }
            u(codeNum, bitLength)
        }

        fun se(value: Int) {
            val codeNum = if (value <= 0) -2 * value else 2 * value - 1
            ue(codeNum)
        }

        fun toBytes(): ByteArray {
            val padded = bits.toMutableList()
            while (padded.size % 8 != 0) padded.add(0)
            val out = ByteArray(padded.size / 8)
            for (i in out.indices) {
                var byte = 0
                for (b in 0 until 8) byte = (byte shl 1) or padded[i * 8 + b]
                out[i] = byte.toByte()
            }
            return out
        }

        /** @param headerByte the NAL header byte [H264Sps.parseDimensions] expects first. */
        fun toSpsNalu(headerByte: Int): ByteArray = byteArrayOf(headerByte.toByte()) + toBytes()
    }
}
