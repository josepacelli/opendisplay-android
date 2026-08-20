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
