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
- **`checkPlayUpdate`** (Play In-App Updates, #208): the app ships on Google Play, so updates go
  through Play. A self-updater that downloads the APK from GitHub (#206) needs
  `REQUEST_INSTALL_PACKAGES`, which Play rejected on upload, and Play forbids a store app updating
  itself from outside the store anyway. On an install that didn't come from Play (GitHub APK,
  `adb`) the `appUpdateInfo` task just fails and nothing happens — those users update from the
  releases page as before. IMMEDIATE, not FLEXIBLE: a flexible update finishes by restarting the
  app, which would cut a live Mac session; the immediate flow runs at launch, before any session.
  Only on a fresh `onCreate` (`savedInstanceState == null`), not on rotation/recreation.
- **`openPendingAccessory`**, `hasPermission` + the `IllegalArgumentException`/`SecurityException`
  catch around `openAccessory`: the Mac resets the device to end a session, so a re-delivered
  launch intent (recents after the process died) can name an attachment that's gone or lost its
  permission — a normal race, not an error (PROTOCOL.md §2.3).
- **`usedAccessory`**: one attachment is one session and MUST NOT be reused (§2.3). Cleared on
  `USB_ACCESSORY_DETACHED`, since an equal-looking accessory after a replug is a new attachment.
- **No `closing` on leaving the foreground**: §2.3 asks a receiver that leaves the foreground
  with the screen on to announce `closing` on the cable so the Mac doesn't redial. Here the session
  lives in `ReceiverService` and survives `onStop` (PiP), so that trigger never happens; `closing`
  only goes out on swipe-away or the notification's Disconnect, after which the Mac won't redial
  until a replug or a manual connect.
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
- **`VideoSurface`**, the `pointerInput(receiver, zoomEnabled)` driver: `awaitEachGesture` is the
  public loop that re-arms one gesture at a time (it drains leftover pointers in between), but it
  rethrows a `CancellationException` that escapes a gesture — and that kills the whole touch
  handler for the rest of the session ("touch dies after a pinch until reconnect"). The outer
  `while` + `try/catch` re-arms it: a cancelled gesture is swallowed and the handler restarted as
  long as the scope is still active; only a real scope cancellation (node removed) is rethrown.
- **`VideoSurface`**, the `LaunchedEffect(zoomEnabled)` reset: turning pinch-zoom off mid-session
  leaves the video stuck zoomed/panned otherwise — scale/pan are remembered Compose state that
  nothing else resets.
- **`VideoSurface`**, the three-finger pan ([GestureMode.PAN]): pans by `panStart + (centroid -
  panStartCentroid)` so the content under the fingers follows the fingers (grab-and-drag), then
  clamps to the same bounds a pinch would allow so the video never uncovers the surface. Local
  only, never sent to the Mac, and gated by the same [zoomEnabled] toggle as the pinch — at
  scale 1 the clamp is `[0, 0]`, so a three-finger drag is a no-op instead of needing a separate
  "is zoomed" check. A third finger landing mid-gesture is only promoted to pan while still
  undecided; a drag/pinch already committed to TOUCH/SCROLL/ZOOM keeps its mode.

## ui/ReceiverScreen.kt

- **`ReceiverScreen`**, the `LaunchedEffect(connected)` that closes Settings: the dialog only
  makes sense while disconnected (see `SettingsDialog`'s doc comment) — if the Mac connects while
  it happens to be open, the now-actively-rendering `VideoSurface` behind it swallows further
  mouse input meant for the dialog, leaving it stuck open. Closing it the moment we connect
  sidesteps that instead of relying on it staying dismissible on top of live video.
- **`ReceiverScreen`**, the scrim `Box` before `IdleContent`: covers the video box — otherwise the
  last decoded frame stays visible behind the status text after a disconnect.
- **`lampStateFor`**: `PhoneReceiver` only exposes the status as a localized string, so the lamp
  colour is picked by comparing it to the same `status_*` resources it was built from. Anything
  that isn't a known problem or starting/stopped counts as listening — that string carries the
  bound port, so it can't be matched exactly. Avoids adding a status enum to `PhoneReceiver`
  just for a colour.
- **`IdleContent`**, `short` (height under 480dp): a phone like the Galaxy A56 on its side is
  ~915x412dp. It keeps the two-column layout — stacking would push the steps and Settings below
  the fold — but drops the panel padding from 56 to 24dp and the name to `displaySmall` so
  everything fits without scrolling.
- **`IndicatorLamp`**, the pulse reads `ANIMATOR_DURATION_SCALE` itself: an infinite transition
  with the system's "remove animations" setting on would otherwise still run. Read once per
  composition; flipping the setting takes effect the next time the idle screen shows.
- **`ReceiverScreen`**, `ConnectionUnstableBanner`'s `Alignment.BottomCenter`: the top of the
  screen already hosts `PeerSignalBanner` and `PerfHud` — bottom keeps this transient pill from
  fighting either for space when a resync happens to land alongside one of them.
- **`ReceiverScreen`**, the `LaunchedEffect(lastActivityAt, connectedMenuIdleSeconds)` that
  reveals `ConnectedMenu`: keying on `lastActivityAt` means every new touch or Mac cursor move
  restarts the `delay` from scratch, so a long drag (or the Mac's mouse just being in motion)
  never gets interrupted by the menu popping back up mid-gesture — it only reappears after
  `connectedMenuIdleSeconds` with no activity of either kind at all. Skipped for the sentinel `0L`
  (just connected, nothing to wait out yet — see the `LaunchedEffect(connected)` above it, which
  seeds that sentinel and shows the menu immediately on connect instead of waiting out the delay
  for no reason).
- **`ReceiverScreen`**, the video `Box`'s `pointerInput(connected)` (Initial pass, `#151`): an
  observer only, never consumes — `VideoSurface`'s own touch-forwarding/pinch-zoom gestures
  underneath are untouched. It's attached to the video `Box`, a sibling of `ConnectedMenu` rather
  than an ancestor of it, so taps on the menu's own grip/buttons never pass through here and so
  never count as "activity" that would hide the menu right after using it.
- **`ReceiverScreen`**, the `LaunchedEffect(cursorPosition)` that also counts as "activity": the
  Mac's mouse cursor moving is driven by `PhoneReceiver.cursorPosition`, a separate signal from
  local touch — a user mousing around on the Mac with the tablet screen otherwise untouched
  should hide the menu too, not just leave it sitting on top of the desktop being controlled.

## ui/theme/Theme.kt

- **Hanken Grotesk as four static TTFs** instead of the variable font: on the test device (Galaxy
  Tab S10 FE+, Android 15) `FontVariation.Settings` on a resource font didn't change the weight —
  every role rendered at the variable font's default. Static instances cut with
  `fonttools varLib.instancer` render the right weight everywhere back to minSdk 26.
- **`OverlayColors`** doesn't follow the light/dark scheme: the floating menu, perf HUD and
  unstable-connection pill sit on top of the Mac's desktop, whose brightness has nothing to do
  with the Android system theme. A fixed dark navy reads on any wallpaper.

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
- **`isAdbEnabled`** as the loopback listener's `acceptGate` (#159): the loopback listener only
  serves the Mac's `adb forward` override, and `adb forward` doesn't work without USB debugging.
  With debugging off, a loopback connection can only come from another app on the device, which
  could otherwise inject video and cursor data with just the `INTERNET` permission. Checked per
  connection rather than at bind time, so turning debugging on mid-run works without restarting
  the service. No protocol change: `adb forward` is Android's own binding (PROTOCOL.md §2).
- **`closeServerSocket`**, the empty catch: the socket was already gone.
- **`acceptConnection`**, the `closeConnection()` call at the top: replaces any existing
  connection, like the Mac replacing its dial.
- **`needsProof` / `proveNewcomer`** (#168): PROTOCOL.md §1 has the receiver adopt a new
  connection and drop the old one. #165 had rejected any other host during a live session
  instead, which diverged. Now it does what the official receiver does
  (`Shared/StreamReceiver.swift`): a newcomer from another address gets a provisional `hello`
  without `cursorPort` and replaces the session only once it sends bytes back. The bytes it
  sent are replayed through `PrefixedLink`, so framing starts at its first byte. This stops
  the twin IPv4/IPv6 dial the Mac races from evicting the winner. It does not keep a hostile
  LAN host out: one that speaks the protocol can still take over, same as on iOS (SCR-001 is an
  accepted risk of the unauthenticated pv 3 protocol). The UI shows the peer-replaced banner
  when that happens.
- **`proveNewcomer`**, `NEWCOMER_PROOF_TIMEOUT_MS` (10 s): the official receiver parks a silent
  newcomer until a session change or `stop()`. Android also drops it after 10 s so a socket
  that connects and goes quiet can't hold a thread and buffer indefinitely. The Mac answers
  `hello` within milliseconds, so a real sender never hits it.
- **`MAX_PENDING_NEWCOMERS`** (4, #156): rather than rate-limiting `accept()`, which would
  refuse a Mac redialing ~1 s after a failure and break §1, only the number of connections
  proving themselves at once is capped. A real Mac needs two (its twin dial); extras are closed
  without touching the live session.
- **`proveNewcomer`**, a TCP newcomer during a USB accessory session: a USB link has no
  address, so any TCP connection has to prove itself and then replaces the cable session, as
  §1 asks.
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
- **`loadPerfHudPosition`**, the `runCatching` around `PerfHudPosition.valueOf`: this value only
  ever comes from what this app itself wrote via `setPerfHudPosition` (#63), so a bad stored value
  isn't expected — but `enum.valueOf` throws on a miss, and the one plausible way to get one (a
  future rename of an enum constant, leaving an old install's prefs pointing at a name that no
  longer exists) shouldn't crash the app over a cosmetic setting. Falls back to the same
  `TOP_START` default a fresh install gets.

## net/Framing.kt

- **`FrameDecoder.ensureCapacity`**, the cap at one maximum-size frame plus header (#156): plain
  doubling took a peer that declares a 16 MiB frame and trickles it in to a 32 MiB buffer.
  `MAX_FRAME_SIZE` itself stays at 16 MiB: the official receiver has no limit at all, and a lower
  cap would drop the whole session on a big legitimate keyframe. Lower it only after measuring
  the largest keyframe the real Mac sends.

## net/PacketInputStream.kt

- **Whole 16 KB reads, 0 bytes is not EOF**: PROTOCOL.md §2.3. The gadget driver keeps one 16 KB
  request in flight and the Mac ends writes that are a multiple of the max packet size with a
  zero-length packet; `FileInputStream` reports that as `-1` and dropped the session on any frame
  sized in multiples of 512. 64 empty reads in a row still end the stream, so a vanished endpoint
  can't spin forever.

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
- **`implementation(libs.androidx.fragment)`**, never used directly: `play:app-update` (#208)
  drags in `androidx.fragment:fragment:1.0.0`, and with a pre-1.3 fragment on the classpath
  release lint fails every `registerForActivityResult` in `MainActivity`
  (`InvalidFragmentVersionForActivityResult`). Pinning a current fragment is the fix lint itself
  recommends.

## web/vite.config.ts

- **`build.emptyOutDir: false`**: the build's `outDir` is `../docs`, which still holds the
  hand-authored static HTML for every locale except pt-BR (`en/`, `es/`, `ja/`, `ko/`, `pt-PT/`,
  `zh-Hans/`) plus their shared root-level `styles.css`/`nav-scroll.js`/`lang-switch.js`/
  `scroll-reveal.js`/`back-to-top.js` (each locale page references those via `../`, see
  `docs/en/index.html`). Emptying `docs/` before each build would wipe all of that; `false` lets
  Vite overwrite only the files this app actually outputs (`index.html`, `code-of-conduct.html`,
  `contributing.html`, `privacy.html`, `assets/`) and leaves the rest of `docs/` alone until the
  other locales migrate to this stack too (issue #104's follow-up).

## web/package.json

- **`prebuild` deletes `../docs/assets` before every build**: verified by real attempt — with
  `emptyOutDir: false` (see `vite.config.ts` above), Vite never cleans its own previous hashed
  output either, so two builds in a row left both old and new `index-*.js`/`src-*.css` sitting in
  `docs/assets/` forever. `docs/assets/` holds only files this build produces (every other
  locale's shared JS/CSS lives at the `docs/` root, not in `assets/`), so wiping just that one
  subfolder first is safe and keeps it from growing without bound.

## web/src/components/sections/Support.tsx

- **Ko-fi rendered as a plain link, not the official `Widget_2.js` embed**: verified by real
  attempt — loading `https://storage.ko-fi.com/cdn/widget/Widget_2.js` via a dynamically injected
  `<script>` (the only option in a React SPA; the original static page could put the `<script>`
  tag directly in the HTML) blanked the entire rendered page. The widget calls `document.write`,
  which implicitly calls `document.open()` first when the document has already finished
  loading — that wipes out everything React already rendered, not just the widget's own markup.
  A static button linking straight to the Ko-fi page sidesteps the whole failure mode.
