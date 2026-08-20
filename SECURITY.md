# Security

## Reporting a vulnerability

This is a small, unfunded open-source project with no bug bounty. If you find a security issue,
please open a [GitHub issue](https://github.com/josepacelli/opendisplay-android/issues) or a
private security advisory on the repository. Include steps to reproduce and, if possible, which
finding ID below (if any) it relates to.

## Threat model, in short

- The Android app **listens** on a raw TCP socket (port 9000, no TLS) and the Mac **connects** to
  it — same protocol as the original [OpenDisplay](https://github.com/peetzweg/opendisplay) Mac
  app, unmodified. This is a deliberate, documented design choice (no account, no central server,
  see the project README) and cannot diverge from it without breaking interoperability with the
  Mac app.
- The trust boundary is **the local network**: anyone who can reach the phone/tablet's IP on the
  same WiFi (or a routed subnet) can connect to port 9000. There is no authentication of the peer
  — this mirrors the upstream iOS client's behavior exactly, not a gap introduced by this port.
- There's also an unauthenticated USB path (`AccessoryLink`, same wire protocol, no peer auth
  either) — but it needs a physical cable, so the trust boundary there is physical access, not the
  network. Same finding set applies to both transports; PhoneReceiver doesn't know which one it's
  talking over.
- Given that boundary, findings below are classified by what an unauthenticated device on the same
  network can actually do, not by internet-facing severity norms.

## Security review — 2026-08-04

Structured review against OWASP ASVS 4.0.3 / CWE Top 25, scoped to the network/protocol layer
(`net/`, `protocol/`, `video/VideoDecoder.kt`, `service/ReceiverService.kt`,
`ui/SettingsDialog.kt`, `ui/CursorOverlay.kt`, `ui/ReceiverScreen.kt`, `AndroidManifest.xml`,
`gradle/libs.versions.toml`).

| ID | Title | Severity | CWE | Status |
|---|---|---|---|---|
| SCR-001 | No peer authentication on the control/video socket | Medium | CWE-306 | Accepted risk (inherited from upstream protocol) — UI now warns when a different peer replaces an active session, but doesn't block it |
| SCR-002 | Cursor sprite bitmap decoded without dimension validation | Medium | CWE-400 | Fixed — bounds-only decode rejects oversized sprites before allocation |
| SCR-003 | Unvalidated `store` URL forwarded from an untrusted peer | Low | CWE-601 | Fixed — validated against an `https://` + host allowlist at ingestion |
| SCR-004 | Persistent install ID broadcast in cleartext over mDNS | Informational | CWE-200 | Accepted risk (required by protocol) |
| SCR-005 | `allowBackup="true"` with no data extraction rules | Informational | CWE-312 | Fixed — `dataExtractionRules` excludes app preferences from backup/transfer |
| SCR-006 | Listener bound to all network interfaces, not just WiFi | Informational | — | Mitigated — binds to loopback (USB path) and the current WiFi IP instead of `0.0.0.0`, no longer all interfaces |

SCR-002, SCR-003 and SCR-005 were fixed outright (see git history for the implementation). SCR-001
and SCR-006 later got mitigations that reduce exposure without removing the underlying risk — a
full fix would mean diverging from the wire protocol the Mac app speaks, which this project cannot
do unilaterally (see `CLAUDE.md`'s "Protocolo — fonte da verdade" section). SCR-004 remains an
accepted risk, unchanged.

## Security review — 2026-08-18

Follow-up pass covering everything added since the review above (notification permission/toggle,
disconnect action, screen-timeout fixes, localization) plus a fresh read of the original scope.
SCR-002 through SCR-006 verified still intact, no regressions.

| ID | Title | Severity | CWE | Status |
|---|---|---|---|---|
| SCR-007 | Unbounded cursor sprite dimensions (`nw`/`nh`) from peer, no upper-bound check | Low | CWE-20 | Fixed (hardening) — see [#34](https://github.com/josepacelli/opendisplay-android/issues/34) |

SCR-007: `PhoneReceiver.handleCursorImage()` read `nw`/`nh`/`ax`/`ay` from the untrusted peer's
`cursorImg` message with no upper-bound check, and `CursorOverlay.kt`'s `CursorSprite` used them
directly to size a `Constraints.fixed(...)` measure call. Initial write-up theorized this could
crash the app (an extreme value like `"nw": 1e9` seemed likely to exceed what Compose's
`Constraints` can represent). **Verified against a real crafted-peer exploit attempt** (a raw
Python TCP client sending `cursor` + a `cursorImg` with `nw=nh=1e9` straight at the unpatched
build) — **it did not crash**: no fatal exception, same process ID throughout, app kept working
normally afterward. `Constraints.fixed(a, a)` with a matching min/max apparently doesn't hit the
same bit-packing ceiling this write-up assumed. Downgraded from the originally-reported Medium
accordingly. Still fixed as defense-in-depth (`nw`/`nh` now clamped to `0.0..4.0` at ingestion,
mirroring the SCR-002 bounds-check pattern) — relying on an external library's undocumented
handling of out-of-range input isn't a real guarantee, even though this specific crash theory
didn't pan out.

## Security review — 2026-08-20

Follow-up pass covering everything added since the review above (USB accessory transport,
WiFi-only listener hardening). SCR-002 through SCR-007 verified still intact, no regressions.

| ID | Title | Severity | CWE | Status |
|---|---|---|---|---|
| SCR-008 | SCR-006's mitigation didn't check interface transport — listener could still bind to a cellular interface | Medium | CWE-284 | Fixed — see [#39](https://github.com/josepacelli/opendisplay-android/issues/39) |

SCR-008: SCR-006's original mitigation (2026-08-04) picked the first "up, non-loopback" network
interface with no regard for its transport. On a device with WiFi off and mobile data on, that's
the carrier's `rmnetX` interface — so the unauthenticated video/control socket ended up bound to
LTE instead of just failing to bind, reopening exactly the exposure SCR-006 was meant to close.
**Verified on real hardware** (WiFi off, LTE on): bound to the carrier-assigned address before the
fix, stayed loopback-only after it. Fixed by gating the bind behind `ConnectivityManager` — only
binds when the active network is WiFi or Ethernet. A companion fix (WiFi-listener now closes
itself within ~1s of WiFi disappearing mid-session, instead of lingering on a dead address) closes
a related reliability gap but isn't itself a new exposure — the socket never re-appeared on
cellular even before that fix, it just reported a stale "Listening" status. USB (`AccessoryLink`,
loopback via `adb forward`) doesn't go through this check at all and was unaffected either way.

## What this app deliberately does not have

No accounts, no telemetry, no central server, no TLS on the wire protocol — same philosophy as
the original OpenDisplay project. Anything that requires the Mac app to change is out of scope
for this repository.
