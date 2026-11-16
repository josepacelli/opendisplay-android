---
name: wire-protocol
description: Reference for the OpenDisplay wire protocol (pv 3) that this Android receiver must speak to interoperate with the unchanged Mac sender. Summarizes upstream PROTOCOL.md (the normative spec) and records which parts Android implements, skips on purpose, or diverges from. Check it before touching networking, handshake, framing or the session lifecycle.
---

# OpenDisplay protocol (reference for the Android receiver)

**Source of truth: upstream `PROTOCOL.md`** in `peetzweg/opendisplay` (normative for `pv <= 3`,
RFC 2119 wording), plus `COMPATIBILITY.md` for how it evolves. This skill is a summary plus the
Android implementation status; when they disagree, PROTOCOL.md wins and this file is stale.
Last checked against upstream commit `45bae1f` (v1.30.3, 2026-11-14); §2.3 (USB/AOA) added 2026-10-24.

```sh
git clone --depth 1 https://github.com/peetzweg/opendisplay.git /tmp/opendisplay-upstream
less /tmp/opendisplay-upstream/PROTOCOL.md      # the spec
less /tmp/opendisplay-upstream/Shared/StreamReceiver.swift   # official receiver behavior
```

The Mac does not change. Any divergence on our side is a silent incompatibility.

## Rule: a change never breaks the protocol (security included)

Before changing networking, handshake, framing or the session lifecycle:

1. Read the matching PROTOCOL.md section and its MUST/MUST NOT rules.
2. Hardening (rejecting a connection, truncating/validating a field, capping a size, dropping a
   message) must not drop a legitimate session or change what goes on the wire. When in doubt,
   degrade instead of rejecting.
3. The rules that most often collide with security work: unknown types and fields are ignored
   (§6); an unparseable control payload is ignored, never fatal; a new connection replaces the
   active one once it sends bytes (§1); silence over 5 s is death and both ends ping every 2 s (§8.2); `hello` is
   always the first message (§6.1); the JSON/video demux stays intact (§4).
4. If a divergence is deliberate, document it in `RATIONALE.md` and in the table below, test
   against the real Mac, and decide it with the owner before merging.

## Roles and transport (§1, §2)

- **Android listens** on TCP **9000** and advertises itself; **the Mac dials**. One TCP
  connection carries video, control and telemetry in both directions. No TLS, no auth at pv 3.
- `TCP_NODELAY` on (`SocketLink`, `net/Link.kt`).
- **One sender at a time.** A new inbound connection MUST be adopted and the old one dropped
  once the newcomer proves itself with bytes (§1, normative since the 2026-11 PROTOCOL.md). The
  official receiver adopts a newcomer immediately when there is no live session; with a live
  session it sends the newcomer a provisional `hello` (without `cursorPort`), keeps serving the
  live sender, and adopts the newcomer when its first bytes arrive, handing those bytes to the
  new session; a newcomer that closes or stays silent is dropped. Adopting resets decoder state
  and sends the full `hello`. This keeps the twin IPv4/IPv6 dial the Mac races from evicting the
  winner (Android: `proveNewcomer`, `PrefixedLink`) (`Shared/StreamReceiver.swift`, listener `newConnectionHandler`; PROTOCOL.md §6.4).
- **Bindings** (§2): WiFi via Bonjour; Apple USB via usbmuxd (not on Android); Android USB via
  AOA (§2.3, `AccessoryLink`, Mac app ≥ v1.28.0). §2.3 MUSTs: filter without `version`; whole
  16 KB reads with a 0-byte read as ZLP not EOF (`PacketInputStream`); one attachment = one
  session (`usedAccessory`). On macOS a running adb server holds every device with USB debugging
  on, so the Mac can't ask it for accessory mode; the Mac (≥ v1.30.2) retries every 3 s and
  lists the phone once `adb kill-server` frees it. Nothing to do on Android (issue #212 is docs
  only). We also keep `adb forward` to the loopback listener (Mac
  `host`/`port` override).

### Bonjour (§2.1)

- Type `_opensidecar._tcp` (historical name, never rename). Name: user-editable display name,
  never an identity.
- TXT: `id` (stable install UUID, MUST equal `hello.id`) and `pv` (`"3"`).

## Framing (§3)

```
[4-byte payload length, unsigned, big-endian][payload]
```

- Receiver → sender payloads MUST be `1` to `2^20 - 1` bytes; the Mac treats 0 or `>= 2^20` as a
  protocol error and stops reading control on that connection.
- Sender → receiver has no hard maximum (keyframes of a large panel run to a few MB). Reassemble
  across reads; frames can arrive split or packed.

## Demux: control JSON or video? (§4, deprecated heuristic)

A sender → receiver frame is control JSON if and only if:

```kotlin
payload.size < 32_768 && payload.isNotEmpty() && payload[0] == '{'.code.toByte() &&
    payload.none { it == 0.toByte() }
```

Video frames can also start with `{` (telemetry prefix) but always contain NULs (start codes).
Keep this decision isolated (`net/Framing.kt`): pv 4 reserves a typed frame header to replace it,
via a two-phase migration. Never build on the heuristic's edge cases.

## Video (§5)

- H.264 Annex B by default; HEVC only after the receiver offers it in `hello.videoCaps` and the
  sender announces it in `streamConfig`. **Android offers no `videoCaps`**, so it always gets
  H.264.
- Frame layout: optional telemetry prefix `{"cap":<ms>,"snd":<ms>}` (sender clock), then NALUs
  with **4-byte** start codes only. Every IDR carries SPS+PPS. One wire frame = one picture =
  one decoder sample. No timestamps on the wire; display in arrival order.
- **Video size comes from the SPS, never from `hello`** (§5.2) — the Mac may stream smaller
  than the panel. A parameter-set change means rebuild the decoder and drop buffered frames
  (`video/H264Sps.kt` reads the real cropped size).
- Decode lost (joined mid-GOP, decoder reset, back from background): send `kf`; the next frame
  is an IDR (§5.3).

## Control messages (§6)

UTF-8 JSON, one object per frame, `type` discriminator. **Unknown types and unknown fields MUST
be ignored** (log unknown types at most once per type, not per message); absent optional fields
tolerated; unparseable payload ignored. New fields arrive without a version bump.

### Android → Mac (§6.1)

| `type` | Fields we send | Notes |
|---|---|---|
| `hello` | `pixelsWide`, `pixelsHigh`, `scale`, `device: "Android"`, `id`, `pv: 3`, `panel`?, `maxEncodeWide/High`?, `cursorPort`? | First message on every connection. Re-sent on rotation, fold, window resize or any panel change (`setPanelSize`, fed by `currentWindowMetrics`). The Mac settles a burst of hellos (25 ms after a lone change, 150 ms after the last of a burst), so no receiver-side debounce. See below. |
| `ping` | `t` (ms since epoch, our clock) | Every 2 s. Mac answers `pong`. |
| `touch` | `phase` (`began`/`moved`/`ended`/`cancelled`), `x`, `y` (normalized video space), `t`? (Mac clock, once offset is known) | |
| `scroll` | `dx`, `dy` | **Video pixels**, natural-scrolling sign. |
| `pencil` | `phase` (`down`/`move`/`up`/`hover`), `x`, `y`, `pressure`, `azimuth`, `altitude`, `rotation`, `t`? | Only to a Mac with `welcome.pv >= 3`; below that degrade the stylus to `touch` (normative). |
| `kf` | — | Request an IDR. |
| `cursorAck` | — | Over TCP, on the first accepted UDP cursor datagram of a flow (§6.3). |
| `sleeping` / `closing` | — | Best-effort courtesies before closing (locked / quit). |

**`hello` fields:**
- `pixelsWide`, `pixelsHigh`, `scale`: **deprecated** (§6.1), removed at the next pv bump. Kept
  until then. A Mac that gets a valid `panel` ignores them.
- `panel` (§6.7): `{pixelsWide, pixelsHigh, scale}` — physical pixels of the window in the
  current orientation and the real density (`HelloPanel` in `protocol/WireProtocol.kt`). Left
  out until the panel size is known. `hello` is re-sent when any of the three changes. `pointsWide/High` omitted (Android has no
  "looks like" desktop setting). The Mac builds a 2x desktop for `scale >= 1.5`, else 1x, and
  ignores the whole object unless pixels are integers `>= 2` and scale is finite `> 0`.
- `maxEncodeWide/High` (§6.5): the AVC decoder's largest supported raster (legacy H.264 decode
  ceiling), in the current orientation.
- `cursorPort` (§6.3): only while the UDP listener is actually bound (TCP port + 1).
- `addrs` (§6.4), `power` (§6.6): Mac-receiver features (host-to-host cable, power off). Android
  never sends them — a phone's WiFi address would invite a false "cable upgrade".
- `videoCaps`, `displayMaxFrameRate` (§6.5): not sent (H.264 only, legacy 60 FPS default).

`proximity` (stylus hover enter/leave) and receiver `stats` are not implemented; both are
optional.

### Mac → Android (§6.2)

| `type` | Fields | What we do |
|---|---|---|
| `welcome` | `pv`, `min` | Store the Mac's `pv` (gates `pencil`). Sent after every `hello`; handle idempotently. No `welcome` at all = Mac `pv` 1. |
| `updateRequired` | `target`, `store`, `message` | Show `message` and a link to `store` (sanitized: only our GitHub/Play URLs, truncated message). Don't rely on video stopping. |
| `pong` | `t`, `mt` | Clock offset (§8.1). |
| `ping` | `encDrops`?, `netDrops`?, `drops`?, `pending`?, `inp50`?, `inp95`?, `capFps`? | Liveness beat; counters are informational. Does **not** solicit a `pong`. |
| `cursor` | `x`?, `y`?, `v` (1/0), `s`? | Local cursor overlay. With `s`, drop anything not greater than the last seen (UDP + TCP share one sequence per TCP session). |
| `cursorImg` | `png` (base64, < 24000 bytes raw), `nw`, `nh` (normalized to display), `ax`, `ay` (normalized to sprite) | Cursor sprite. |
| `streamConfig` | `codec`, `width`, `height`, `framesPerSecond` | Not handled (ignored as unknown). Absent or ignored = legacy H.264, which is all we decode. |
| `power` | `action` | Ignored: we never list `hello.power`. |
| `stats` | free-form | Logged as `MAC-STATS`. |

### UDP cursor side channel (§6.3)

Optional, WiFi only (a USB/`adb forward` session can't carry it). One datagram = one `cursor`
JSON without length prefix, plus `s`. Accept datagrams only from the most recent flow, reset the
sequence on every new TCP connection, send `cursorAck` over TCP on the first accepted datagram.
Android also accepts datagrams only from the TCP peer's address (`isTrustedCursorSource`).
Withdraw the offer by re-sending `hello` without `cursorPort`.

## Coordinates and units (§7)

| What | Space | Units |
|---|---|---|
| `hello.pixelsWide/High` (deprecated) | desired desktop | pixels of a 2x desktop, current orientation |
| `hello.panel.pixelsWide/High` | physical panel | pixels, current orientation |
| `hello.panel.scale` | — | real backing scale, may be fractional |
| `touch`/`pencil`/`cursor` `x`,`y` | video | normalized 0..1, top-left origin |
| `scroll.dx/dy` | video | **pixels**, natural sign |
| `cursorImg.nw/nh` | display | normalized to display size |
| `cursorImg.ax/ay` | sprite | normalized to sprite size |
| `pencil.azimuth/altitude/rotation` | physical | radians, altitude π/2 = perpendicular |
| `ping.t`, `pong.t/mt`, `cap`/`snd`, `touch.t` | wall clock | ms since epoch |

## Time and liveness (§8)

- Clock sync: `rtt = t2 - t1`, `offset = mt - (t1 + t2) / 2`; discard `rtt < 0` or
  `>= 2000 ms`, keep the last 15, use the offset of the minimum-RTT sample. Feeds the latency
  HUD and `touch.t`/`pencil.t`.
- **Liveness (normative):** both official apps drop the link after more than 5 s without any
  bytes, so each end must send something at least every ~5 s. Ping every 2 s. Android's watchdog
  drops the session after 5 s of silence.
- Reconnects are the Mac's job: it redials ~1 s after a failure, 5 s per dial, 10 s grace for a
  known device; ends sooner after `closing`, repeated refused dials, or a withdrawn Bonjour
  service.

## Session lifecycle (§9)

`hello` first on every connection → Mac sizes the display → `welcome` → (`updateRequired` if
`hello.pv < welcome.min`) → `streamConfig`? → IDR first, then video, `cursorImg`/`cursor` → pings
both ways every 2 s → input → `kf` when decode is lost → re-`hello` on rotation (no reconnect) →
`sleeping`/`closing` best-effort → close.

## Versioning (§10)

`pv` is one integer, bumped only when the wire changes (current **3**). No `pv` anywhere = 1.
Additive changes (new optional fields/types) need no bump. Breaking changes are two-phase.
Reserved for pv 4: typed frame header replacing the §4 heuristic.

```kotlin
object WireProtocol {
    const val VERSION = 3
    const val MIN_SUPPORTED_PEER = 1
    const val ASSUMED_WHEN_ABSENT = 1
    const val PENCIL_MIN_PEER = 3
}
```

## Android divergences and choices (keep this table current)

| Area | Android behavior | Status |
|---|---|---|
| Newcomer while a session is live (§1) | Same host: adopted at once. Other host: provisional `hello` without `cursorPort`, adopted once it sends bytes, dropped after 10 s of silence (`needsProof`/`proveNewcomer`, #168) | Conformant, matches the official receiver; the 10 s cap is ours |
| UDP cursor source | Only datagrams from the TCP peer's address | Stricter than spec, no wire change |
| `updateRequired.store`/`message` | URL allow-list, message truncated | Display-side only |
| `hello.videoCaps` / HEVC / `streamConfig` | Not offered / ignored | Optional, legacy H.264 |
| `proximity`, receiver `stats` | Not sent | Optional |
| `addrs`, `power` | Never sent | Mac-receiver only by design |
| Bindings | WiFi, AOA accessory (§2.3), `adb forward` loopback | AOA is normative; forward is allowed by §2 |

## Mac-side behavior worth knowing

- `InputInjector` (`Mac/InputInjector.swift`): `began` → mouse down, `moved` → drag, `ended`/
  `cancelled` → mouse up; `scroll` → scroll-wheel `CGEvent` in pixels.
- **Mirror mode is view-only.** `MacSender` only creates the `InputInjector` on the Extend path,
  tied to the virtual display; in Mirror, `touch`/`scroll` arrive and are ignored. Touch in
  Mirror is a feature request for `peetzweg/opendisplay`, not a client bug.
