# Rationale

Implementation notes that used to live as inline `//` comments in the code — the non-obvious
*why* behind a specific line or block, moved here to keep the source itself free of mid-function
comments (see issue #42). KDoc on classes/functions stays in the code; this file is for the
smaller, line-level decisions that don't belong on a function signature.

Organized by file, in source order.

## MainActivity.kt

- **`notificationPermissionLauncher`** (no-op result handler): if denied, the foreground service
  still runs fine — only its persistent notification (status/version/disconnect, see #22)
  silently won't show, same as before this permission request existed.
- **`openPendingAccessory`**, `manager?.openAccessory(accessory)` returning `null`: means the
  cable left before we got here — a normal race, not an error.
- **`onCreate`**, the `takeAccessory(intent)` call before binding: the launch intent may carry the
  accessory; it's claimed once the service binds.
- **`onCreate`**, the `FLAG_KEEP_SCREEN_ON` effect: mirrors iOS's `isIdleTimerDisabled = true` —
  this app IS the screen while a Mac is actually mirroring, so the system's inactivity timeout
  must not fire mid-session (issue #10). Only while connected, though — idle/waiting should time
  out normally like any other app (issue #28). Doesn't block a manual power-button lock either
  way — screen lock/unlock still goes through `PhoneReceiver.enterSleep()`/`wake()` via
  `ReceiverService`'s `SCREEN_OFF`/`SCREEN_ON` receiver.

## ui/VideoSurface.kt

- **`VideoSurface`**, the frame-collecting `LaunchedEffect`: feeds every decoded frame to
  whichever decoder is currently attached — `decoder` is read live through the closure each
  emission, so this one collector survives surface create/destroy cycles.
- **`surfaceCreated`**, the `requestKeyframe()` call: a brand-new decoder has nothing to render
  until it sees a keyframe — without asking, it just sits black until the Mac's own periodic one,
  up to 60s away (see `VideoDecoder`'s `onError` doc in [VideoDecoder.kt](app/src/main/java/io/github/josepacelli/opendisplay/video/VideoDecoder.kt)).
  Every surface recreation (e.g. backgrounding the app and returning) hit exactly this with no
  error involved, so ask immediately instead of waiting.
- **`surfaceCreated`**, why only `onError` (not this same `requestKeyframe()` call) also flips
  `notifyConnectionUnstable()`: a fresh surface is routine (backgrounding/PiP/rotation), not a sign
  of a flaky connection — the banner is for the case that's actually diagnostic of bad WiFi (#61).
- **`handleTouchAndScroll`**, `UNDECIDED` state, single pointer under slop: still inside the slop
  — stay `UNDECIDED` and keep waiting.
- **`handleTouchAndScroll`**, `UNDECIDED` state, pointer lifted: lifted before crossing slop or
  gaining a second pointer — a tap.

## ui/ReceiverScreen.kt

- **`ReceiverScreen`**, the `LaunchedEffect(connected)` that closes Settings: the dialog only
  makes sense while disconnected (see `SettingsDialog`'s doc comment) — if the Mac connects while
  it happens to be open, the now-actively-rendering `VideoSurface` behind it swallows further
  mouse input meant for the dialog, leaving it stuck open. Closing it the moment we connect
  sidesteps that instead of relying on it staying dismissible on top of live video.
- **`ReceiverScreen`**, the scrim `Box` before `IdleContent`: covers the video box — otherwise the
  last decoded frame stays visible behind the status text after a disconnect.
- **`IdleContent`**, the app-icon `Box`: the app icon is an adaptive icon (mipmap XML with
  separate background/foreground layers) — `painterResource` can't load that directly, so this
  recreates the round launcher look from its layers.
- **`IdleContent`**, the status-dot `Row`: this content only renders while disconnected, so the
  dot mirrors the iOS `IdleView`'s semantics (green = connected) by being orange here by
  construction, not a value read from `status`.
- **`ReceiverScreen`**, `ConnectionUnstableBanner`'s `Alignment.BottomCenter`: the top of the
  screen already hosts `PeerSignalBanner` and `PerfHud` — bottom keeps this transient pill from
  fighting either for space when a resync happens to land alongside one of them.

## video/VideoDecoder.kt

- **`submit`**, `codec ?: return`: no SPS/PPS yet — nothing to feed until the first keyframe.
- **`reconfigure`**, `KEY_PRIORITY = 0`: realtime priority.
- **`queueAccessUnit`**, non-blocking `dequeueInputBuffer`: low latency means "latest frame
  wins" — if the codec is momentarily busy, drop rather than wait, mirroring the Mac encoder's
  own `pendingEncodes` backpressure. But dropping a NAL isn't free on H.264: P-frames reference
  the previous decoded frame, so a dropped access unit corrupts every frame after it until a
  fresh IDR arrives — request one instead of waiting for the Mac's own periodic keyframe (up to
  60s away).
- **`drainOutput`**, `releaseOutputBuffer(outIndex, true)`: render ASAP.
- **`drainOutput`**, the fallback `else -> return`: covers `INFO_TRY_AGAIN_LATER` or the
  deprecated buffers-changed code.
- **`drainOutput`**, `if (!spsDimensionsKnown) onSizeChanged(...)`: only trust
  `INFO_OUTPUT_FORMAT_CHANGED` when the SPS itself couldn't be parsed. Some decoders (Qualcomm's
  C2 AVC decoder, at least — see issue #44) echo `MediaFormat`'s configured seed size back through
  this callback instead of the real coded size, which would otherwise silently overwrite a
  correct SPS-derived aspect ratio with a wrong one on every frame.
- **`submit`**, the `frame.seq` gap check: `queueAccessUnit`'s dequeue-side drop (above) isn't the
  only place a frame can go missing — `PhoneReceiver.videoFrames` is a `SharedFlow` with a 4-frame
  `DROP_OLDEST` buffer, so a burst after a WiFi stall (many frames arriving at once once the TCP
  stream catches up) can silently evict frames before they ever reach this decoder. Same failure
  mode as a dropped NAL — broken reference chain, garbled picture — so it gets the same fix: notice
  the gap in the monotonic `seq` and ask for a keyframe instead of waiting up to 60s.
- **`submit`**, `justReconfigured` suppressing the gap check for one frame: `reconfigure()` runs
  synchronously (MediaCodec `configure`/`start`), and frames keep arriving from the Mac the whole
  time it's blocking — confirmed live (#61), the very next frame after a fresh IDR routinely has a
  seq gap from that alone, no WiFi trouble involved. Reporting that one as a resync would show the
  "unstable connection" banner on every normal reconnect, so only gaps *after* the first
  post-reconfigure frame count as real signal.
- **`signalDesync`**, `desyncCount` resetting after `DESYNC_EPISODE_GAP_MS`: the "unstable
  connection" banner (#61) only means something if it tracks a *current* run of trouble — a
  lifetime total would eventually cross the banner's threshold from isolated blips scattered
  across an hour-long session, none of which reflect the connection's state right now. Resetting
  after 5s of quiet makes the count mean "how bad is it in this episode", which is what the banner
  is supposed to answer.

## net/PhoneReceiver.kt

- **`start`**, the loopback listener's bind address: IPv4 explicitly, not
  `InetAddress.getLoopbackAddress()` (returns `::1` on this hardware) — the Mac's `adb forward`
  override dials `127.0.0.1` specifically.
- **`closeServerSocket`**, the empty catch: the socket was already gone.
- **`acceptConnection`**, the `closeConnection()` call at the top: replaces any existing
  connection, like the Mac replacing its dial.
- **`readLoop`**, the local `FrameDecoder`: owned entirely by this connection's read coroutine —
  no sharing across reconnects, so no locking is needed around its mutable state.
- **`readLoop`**, the `finally` guard (`if (link === current)`): a newer session replacing this
  one already closed `current` via `closeConnection()` — this avoids racing that close.
- **`recordPerfSample`**, the `RTT_UNSTABLE_MS`/`E2E_P95_UNSTABLE_MS` check: a second, independent
  trigger for the "unstable connection" banner (#61) alongside `VideoDecoder`'s desync count —
  reported live against a real degraded connection where the picture visibly lagged ("demorava
  para atualizar") without ever corrupting a frame, so the desync-count path alone never fired.
  RTT/e2e latency catch a WiFi problem before it gets bad enough to actually drop a frame. `fps`
  deliberately isn't part of this check: there's no universal "normal" fps to compare against —
  it's whatever rate the Mac chooses to encode at — so a flat floor would either be a guess or need
  a per-session baseline, both more complexity than this warranted. 250ms RTT / 500ms e2e p95 are
  well past what a healthy LAN WiFi connection should ever show, on the same order of magnitude as
  the existing `WATCHDOG_TIMEOUT_MS`/keyframe-request bounds elsewhere in this file.
- **`handleControlJson`**, the `WireMessage.PING` branch: this is the Mac's OWN liveness ping
  (separate from ours) — it carries its send-side health (`encDrops`/`netDrops`/`pending`/`capFps`)
  for its own HUD equivalent. Confirmed live against the real Mac app: it pings every ~2s
  regardless of whether we ping it. Nothing to reply with here — only *our* ping expects a pong
  back.
- **`handleCursorImage`**, clamping `nw`/`nh`: clamped, not just defaulted, because these size a
  Compose `Layout` measure call in `CursorOverlay`, and an untrusted peer sending something wild
  like `"nw": 1e9` would push that past what `Constraints.fixed()` can represent — a same-LAN
  crash, no auth needed (SECURITY.md/SCR-007). `4.0` is generous headroom over anything the real
  Mac app sends.
- **`sendControl`**, why it dispatches instead of writing inline: callers include UI-thread touch
  handlers that must never block on a stalled socket.

## net/Link.kt

- **`SocketLink` init**, `tcpNoDelay = true`: disables Nagle — touch/scroll are small,
  latency-sensitive packets.
- **`AccessoryLink.close()`**, the three separate try/catch blocks: one failing must not skip the
  others.

## service/ReceiverService.kt

- **`screenReceiver`**, `ACTION_SCREEN_ON` (not `ACTION_USER_PRESENT`): Android only sends
  `USER_PRESENT` when a *secure* keyguard is actually dismissed, which some devices (no secure
  lock, some OEM power-saving paths — reproduced on a Samsung One UI tablet) never fire on a
  plain screen-on, leaving the receiver stuck "Stopped" forever (issue #20). `SCREEN_ON` always
  fires.
- **`onCreate`**, the `combine(...).collect { updateNotification() }`: status/connection changes
  need to keep the notification live because it's the only UI visible while the app isn't in the
  foreground.
- **`onStartCommand`**, the default `receiver.start()` branch: idempotent — safe to call even if
  already running.
- **`registerScreenReceiver`**, `RECEIVER_NOT_EXPORTED`: `SCREEN_OFF`/`SCREEN_ON` are protected
  system broadcasts — no other app needs to (or should be able to) send us a fake one.
- **`unregisterScreenReceiverIfNeeded`**, the caught `IllegalArgumentException`: means it was
  already unregistered.
- **`updateNotification`**, the `showNotification.value` branch: `startForeground()` always needs
  *a* notification to satisfy the OS contract (see `onCreate`) — turning the setting off just
  cancels it right back out from the shade; the foreground service itself is unaffected either
  way.
- **`buildNotification`**, the status icon: TODO (fase 9) — dedicated status icon instead of the
  current system placeholder; needs a real launcher-style small icon.

## app/build.gradle.kts

- **`composeCompiler`**, `includeComposeMappingFile.set(false)`: this Compose compiler feature
  (new in Kotlin 2.3) resolves its mapping-generator artifact against AGP 9.2's built-in Kotlin
  version (2.2.10) instead of the `kotlin.plugin.compose` version this project actually applies
  (2.3.20) — and `compose-group-mapping:2.2.10` was never published, so `assembleRelease` fails
  outright with `isMinifyEnabled = true` unless this is off. Verified by real attempt: confirmed
  via Maven metadata that the feature only started publishing from 2.3.0. Deobfuscated Compose
  stack traces aren't worth losing release builds over until AGP's built-in Kotlin support
  catches up.
