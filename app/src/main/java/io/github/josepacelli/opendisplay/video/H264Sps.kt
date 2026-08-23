package io.github.josepacelli.opendisplay.video

/**
 * Minimal H.264 SPS Exp-Golomb parser — extracts only the coded picture
 * width/height (post-cropping), not the full syntax tree. Needed because
 * `MediaCodec.outputFormat`'s `KEY_WIDTH`/`KEY_HEIGHT` cannot be trusted on
 * every decoder to reflect the real bitstream size rather than the
 * configured seed — see [VideoDecoder] and RATIONALE.md (issue #44).
 *
 * Pure byte/bit manipulation, no Android dependency, unit-testable on the JVM.
 */
object H264Sps {

    data class Dimensions(val width: Int, val height: Int)

    private val PROFILES_WITH_CHROMA_INFO =
        setOf(100, 110, 122, 244, 44, 83, 86, 118, 128, 138, 139, 134, 135)

    /**
     * @param sps one SPS NALU, header byte first, as produced by [io.github.josepacelli.opendisplay.net.AnnexB.parse].
     * @return the coded frame size after cropping, or `null` if the SPS is malformed or uses
     * syntax this parser doesn't handle (caller falls back to the decoder-reported size).
     */
    fun parseDimensions(sps: ByteArray): Dimensions? {
        if (sps.size < 4) return null
        return try {
            val reader = BitReader(stripEmulationPrevention(sps))
            val profileIdc = reader.u(8)
            val constraintFlagsAndReserved = reader.u(8)
            val levelIdc = reader.u(8)
            val seqParameterSetId = reader.ue()

            var chromaFormatIdc = 1
            if (profileIdc in PROFILES_WITH_CHROMA_INFO) {
                chromaFormatIdc = reader.ue()
                if (chromaFormatIdc == 3) {
                    val separateColourPlaneFlag = reader.u(1)
                }
                val bitDepthLumaMinus8 = reader.ue()
                val bitDepthChromaMinus8 = reader.ue()
                val qpprimeYZeroTransformBypassFlag = reader.u(1)
                val seqScalingMatrixPresentFlag = reader.u(1)
                if (seqScalingMatrixPresentFlag == 1) {
                    val listCount = if (chromaFormatIdc != 3) 8 else 12
                    repeat(listCount) { i ->
                        val scalingListPresentFlag = reader.u(1)
                        if (scalingListPresentFlag == 1) skipScalingList(reader, if (i < 6) 16 else 64)
                    }
                }
            }

            val log2MaxFrameNumMinus4 = reader.ue()
            when (val picOrderCntType = reader.ue()) {
                0 -> reader.ue()
                1 -> {
                    val deltaPicOrderAlwaysZeroFlag = reader.u(1)
                    val offsetForNonRefPic = reader.se()
                    val offsetForTopToBottomField = reader.se()
                    repeat(reader.ue()) { reader.se() }
                }
            }

            val maxNumRefFrames = reader.ue()
            val gapsInFrameNumValueAllowedFlag = reader.u(1)
            val picWidthInMbsMinus1 = reader.ue()
            val picHeightInMapUnitsMinus1 = reader.ue()
            val frameMbsOnlyFlag = reader.u(1)
            if (frameMbsOnlyFlag == 0) {
                val mbAdaptiveFrameFieldFlag = reader.u(1)
            }
            val direct8x8InferenceFlag = reader.u(1)

            var cropLeft = 0
            var cropRight = 0
            var cropTop = 0
            var cropBottom = 0
            if (reader.u(1) == 1) {
                cropLeft = reader.ue()
                cropRight = reader.ue()
                cropTop = reader.ue()
                cropBottom = reader.ue()
            }

            val subWidthC = if (chromaFormatIdc == 1 || chromaFormatIdc == 2) 2 else 1
            val subHeightC = if (chromaFormatIdc == 1) 2 else 1
            val cropUnitX = if (chromaFormatIdc == 0) 1 else subWidthC
            val cropUnitY = if (chromaFormatIdc == 0) 2 - frameMbsOnlyFlag else subHeightC * (2 - frameMbsOnlyFlag)

            val width = (picWidthInMbsMinus1 + 1) * 16 - cropUnitX * (cropLeft + cropRight)
            val height = (2 - frameMbsOnlyFlag) * (picHeightInMapUnitsMinus1 + 1) * 16 -
                cropUnitY * (cropTop + cropBottom)

            if (width <= 0 || height <= 0) null else Dimensions(width, height)
        } catch (e: Exception) {
            null
        }
    }

    private fun skipScalingList(reader: BitReader, size: Int) {
        var lastScale = 8
        var nextScale = 8
        repeat(size) {
            if (nextScale != 0) {
                nextScale = (lastScale + reader.se() + 256) % 256
            }
            if (nextScale != 0) lastScale = nextScale
        }
    }

    /** Removes `emulation_prevention_three_byte` (0x03 after any 00 00) and the
     * leading NAL header byte, leaving the raw RBSP bit sequence. */
    private fun stripEmulationPrevention(nalu: ByteArray): ByteArray {
        val out = ArrayList<Byte>(nalu.size - 1)
        var zeroRun = 0
        for (i in 1 until nalu.size) {
            val b = nalu[i]
            if (zeroRun >= 2 && b == 0x03.toByte()) {
                zeroRun = 0
                continue
            }
            out.add(b)
            zeroRun = if (b == 0.toByte()) zeroRun + 1 else 0
        }
        return out.toByteArray()
    }

    private class BitReader(private val data: ByteArray) {
        private var bitPos = 0

        fun u(n: Int): Int {
            var value = 0
            repeat(n) { value = (value shl 1) or bit() }
            return value
        }

        fun ue(): Int {
            var leadingZeros = 0
            while (bit() == 0) {
                leadingZeros++
                if (leadingZeros > 32) error("malformed exp-golomb code")
            }
            var value = 1
            repeat(leadingZeros) { value = (value shl 1) or bit() }
            return value - 1
        }

        fun se(): Int {
            val codeNum = ue()
            val sign = if (codeNum % 2 == 0) -1 else 1
            return sign * ((codeNum + 1) / 2)
        }

        private fun bit(): Int {
            val byteIndex = bitPos / 8
            if (byteIndex >= data.size) error("out of bits")
            val bitIndex = 7 - (bitPos % 8)
            bitPos++
            return (data[byteIndex].toInt() shr bitIndex) and 1
        }
    }
}
