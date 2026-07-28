package com.peetzweg.opensidecar.android.protocol

/**
 * Wire-protocol version contract, ported from the upstream Shared/Protocol.swift.
 * The Mac app is the fixed peer here — these values must match it exactly,
 * see .claude/skills/wire-protocol/SKILL.md.
 */
object WireProtocol {
    const val VERSION = 2
    const val MIN_SUPPORTED_PEER = 1
    const val ASSUMED_WHEN_ABSENT = 1
}

/** Control-message `type` string constants (mirrors Swift `WireMessage`). */
object WireMessage {
    const val HELLO = "hello"
    const val PING = "ping"
    const val PONG = "pong"
    const val TOUCH = "touch"
    const val SCROLL = "scroll"
    const val KEYFRAME_REQUEST = "kf"
    const val CURSOR = "cursor"
    const val CURSOR_IMAGE = "cursorImg"
    const val STATS = "stats"
    const val WELCOME = "welcome"
    const val UPDATE_REQUIRED = "updateRequired"
    const val SLEEPING = "sleeping"
    const val CLOSING = "closing"
}
