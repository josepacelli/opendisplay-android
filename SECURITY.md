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

## What this app deliberately does not have

No accounts, no telemetry, no central server, no TLS on the wire protocol — same philosophy as
the original OpenDisplay project. Anything that requires the Mac app to change is out of scope
for this repository.
