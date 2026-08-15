# Play Store listing — draft

Copy-paste source for the Play Console store listing. Not part of the app or the website build —
kept here for reference/versioning only.

## Short description (max 80 chars)

```
Use your Android as a real second monitor for your Mac. Open source, no account.
```
(80/80 chars)

## Full description (max 4000 chars)

```
Turn your Android tablet or phone into a real external monitor for your Mac — the same idea as
Sidecar or Duet Display, but free and open source.

OpenDisplay Android is an unofficial client for OpenDisplay, a macOS app that creates a virtual
display and streams it as low-latency H.264 video. The official project only ships an iOS/iPadOS
client; this one speaks the exact same network protocol from Android, with zero changes required
on the Mac side.

HOW IT WORKS
Your Mac runs the original OpenDisplay app (unmodified) and captures a virtual display. Your
Android device listens on the local network and shows up automatically via WiFi discovery — no
pairing code, no manual IP entry needed in the common case. Pick it from the Mac's display list
and it becomes a real extended desktop.

FEATURES
• Hardware H.264 video decoding, straight to the screen — built for low latency.
• Automatic discovery on your WiFi network (mDNS), no manual setup required.
• Touch and scroll: one finger acts as a mouse click/drag, two fingers scroll.
• Remote cursor overlay that mirrors the Mac's actual pointer.
• Foreground service keeps the connection alive in the background and resumes correctly after
  unlocking the screen.
• Manual IP:port connection as a fallback when automatic discovery doesn't work on your network.
• Optional performance HUD (fps, latency, round-trip time) for troubleshooting.
• Adaptive interface for both phones and tablets.

WHAT THIS APP DOES NOT DO
No account, no sign-up, no subscription, no ads, no analytics, no telemetry, no cloud server of
any kind. Every connection is a direct, local link between your Android device and your own Mac.
See the in-app privacy policy for details.

REQUIREMENTS
• Android 8.0 (API 26) or newer.
• A Mac running the original OpenDisplay app, on the same WiFi network (or connected via USB
  through adb, for advanced users).

This is an independent, community-maintained project, not affiliated with the original
OpenDisplay author. It implements the same open network protocol to interoperate with the
official Mac app without requiring any change to it. Source code, issue tracker and full
documentation: https://github.com/josepacelli/opendisplay-android
```
(≈2000/4000 chars — well under the limit, room to grow)

## Notes for Play Console fields

- **Category**: Tools (alternative: Productivity).
- **Privacy policy URL**: `https://josepacelli.github.io/opendisplay-android/privacy.html`
- **Contact email**: use whatever address you want publicly attached to the listing (Play
  requires one; GitHub profile alone isn't enough).
- **Data safety form**: answer "No data collected" across the board — matches the privacy policy
  (no accounts, no analytics, no data leaves the local Mac↔Android connection).
- **Ads**: No.
- **Target audience**: general/adults — no content aimed at children, so the "designed for
  families"/child-directed flow doesn't apply.
- **App access**: no login required — mark as fully accessible without special access
  instructions for the reviewer.
