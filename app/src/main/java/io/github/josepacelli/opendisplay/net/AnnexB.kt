// Derived from PhoneReceiver.swift's handleAnnexB in peetzweg/opendisplay.
// Copyright (c) 2026 Philip Poloczek. Licensed under GPL-3.0.

package io.github.josepacelli.opendisplay.net

/**
 * Parsing of one already-deframed wire payload: is it a JSON control message
 * or an H.264 Annex-B video frame, and if video, what NALUs does it contain.
 * Ported from `PhoneReceiver.swift`'s `handleAnnexB`. See
 * .claude/skills/wire-protocol/SKILL.md for the exact rules this must match.
 *
 * Pure byte/text manipulation, no Android dependency, unit-testable on the JVM.
 */
object AnnexB {

    private const val JSON_SNIFF_MAX_SIZE = 32_768
    private const val OPEN_BRACE = '{'.code.toByte()

    /**
     * Video frames also start with `{` (the telemetry prefix) but always
     * contain NUL bytes (the `00 00 00 01` start codes) — a pure JSON payload
     * never does, even one carrying base64 PNG data.
     *
     * @param payload one deframed wire payload.
     * @return `true` if [payload] is a JSON control message rather than a video frame.
     */
    fun isControlJson(payload: ByteArray): Boolean {
        if (payload.isEmpty() || payload.size >= JSON_SNIFF_MAX_SIZE) return false
        if (payload[0] != OPEN_BRACE) return false
        return payload.none { it == 0.toByte() }
    }

    /** One NALU's type nibble (low 5 bits of the header byte).
     * @param nalu a single NAL unit, header byte first.
     * @return the NAL unit type (e.g. `7` = SPS, `8` = PPS, `6` = SEI). */
    fun naluType(nalu: ByteArray): Int = nalu[0].toInt() and 0x1F

    data class ParsedFrame(
        /** Bytes before the first start code, if any — the sender's capture/send
         * timestamp prefix (`{"cap":...,"snd":...}`), absent on frames with no
         * leading telemetry. */
        val telemetryPrefix: ByteArray?,
        /** Present only on frames carrying a (possibly new) SPS, e.g. keyframes. */
        val sps: ByteArray?,
        /** Present only on frames carrying a (possibly new) PPS. */
        val pps: ByteArray?,
        /** Slice NALUs (everything that isn't SPS/PPS/SEI), in wire order. All
         * slices of one wire payload belong to a single decoded picture. */
        val vclNalus: List<ByteArray>,
    )

    /**
     * Splits a non-JSON payload into NALUs on 4-byte start codes
     * (`00 00 00 01` — the only form this project's sender emits) and
     * classifies them. Stateless: comparing against the previously-seen
     * SPS/PPS to decide whether the decoder needs rebuilding is the caller's
     * job (it requires session state this parser deliberately doesn't hold).
     *
     * @param payload one deframed, non-JSON wire payload (a video frame).
     * @return the frame split into its telemetry prefix, SPS/PPS (if present), and slice NALUs.
     */
    fun parse(payload: ByteArray): ParsedFrame {
        val nalus = mutableListOf<ByteArray>()
        var naluStart = -1
        var firstStartCode = -1
        var i = 0
        while (i + 4 <= payload.size) {
            if (payload[i] == 0.toByte() && payload[i + 1] == 0.toByte() &&
                payload[i + 2] == 0.toByte() && payload[i + 3] == 1.toByte()
            ) {
                if (firstStartCode == -1) firstStartCode = i
                if (naluStart != -1 && naluStart < i) {
                    nalus.add(payload.copyOfRange(naluStart, i))
                }
                naluStart = i + 4
                i += 4
            } else {
                i += 1
            }
        }
        if (naluStart != -1 && naluStart < payload.size) {
            nalus.add(payload.copyOfRange(naluStart, payload.size))
        }
        val telemetryPrefix = if (firstStartCode > 0) payload.copyOfRange(0, firstStartCode) else null

        var sps: ByteArray? = null
        var pps: ByteArray? = null
        val vcl = mutableListOf<ByteArray>()
        for (nalu in nalus) {
            if (nalu.isEmpty()) continue
            when (naluType(nalu)) {
                7 -> sps = nalu
                8 -> pps = nalu
                6 -> { /* SEI — skipped, matches upstream */ }
                else -> vcl.add(nalu)
            }
        }
        return ParsedFrame(telemetryPrefix, sps, pps, vcl)
    }

    data class Telemetry(val captureMs: Long?, val sendMs: Long?)

    private val CAP_FIELD = Regex("\"cap\"\\s*:\\s*(-?\\d+)")
    private val SND_FIELD = Regex("\"snd\"\\s*:\\s*(-?\\d+)")

    /**
     * Extracts `cap`/`snd` from a telemetry prefix without pulling in a JSON
     * parser — the prefix's shape is fixed (`{"cap":<ms>,"snd":<ms>}`), so a
     * couple of regexes keep this module dependency-free and unit-testable.
     *
     * @param prefix the frame's telemetry prefix bytes, or `null` if it had none.
     * @return the extracted `cap`/`snd` timestamps, each `null` if absent or unparsable.
     */
    fun parseTelemetry(prefix: ByteArray?): Telemetry {
        if (prefix == null) return Telemetry(null, null)
        val text = String(prefix, Charsets.UTF_8)
        val cap = CAP_FIELD.find(text)?.groupValues?.get(1)?.toLongOrNull()
        val snd = SND_FIELD.find(text)?.groupValues?.get(1)?.toLongOrNull()
        return Telemetry(cap, snd)
    }
}
