// Derived from iOS/PhoneReceiver.swift in peetzweg/opendisplay.
// Copyright (c) 2026 Philip Poloczek. Licensed under GPL-3.0.

package io.github.josepacelli.opendisplay.net

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Base64
import io.github.josepacelli.opendisplay.R
import io.github.josepacelli.opendisplay.protocol.WireMessage
import io.github.josepacelli.opendisplay.protocol.WireProtocol
import io.github.josepacelli.opendisplay.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

/** `x`/`y` normalized 0-1 in video space, origin top-left. */
data class CursorPosition(val x: Double, val y: Double, val visible: Boolean)

data class CursorImage(
    val png: ByteArray,
    val normalizedWidth: Double,
    val normalizedHeight: Double,
    val anchorX: Double,
    val anchorY: Double,
)

/** One wire video frame, ready for the decoder: telemetry-stripped, NALUs classified.
 * `sps`/`pps` are non-null only on frames that (re)carry them — the receiver still
 * needs to remember the last-seen pair across frames to know when to rebuild. */
data class VideoFrame(
    val sps: ByteArray?,
    val pps: ByteArray?,
    val vclNalus: List<ByteArray>,
    val captureMs: Long?,
    val sendMs: Long?,
)

sealed class PeerSignal {
    /** The connected Mac's protocol is below what we require — issue #132-style gate. */
    data class UpdateMac(val message: String) : PeerSignal()

    /** The Mac refuses this pairing until this Android app updates. */
    data class UpdateAndroid(val message: String, val storeUrl: String?) : PeerSignal()

    /** A different device connected while one was already active. The socket has no peer
     * authentication (see SECURITY.md/SCR-001), so this can't be prevented — only surfaced. */
    data class PeerReplaced(val previousAddress: String, val newAddress: String) : PeerSignal()
}

/** One second window of pipeline health — trimmed-down parity with the iOS
 * receiver's `PerfStats` (fps, true end-to-end latency, control-channel RTT).
 * `e2eP50Ms`/`e2eP95Ms` only include frames where the clock offset is already
 * known; before that (right after connect) they read 0. */
data class PerfStats(
    val fps: Int = 0,
    val e2eP50Ms: Double = 0.0,
    val e2eP95Ms: Double = 0.0,
    val rttMs: Double = 0.0,
)

/**
 * The Android side of the OpenDisplay socket: listens on TCP :9000, advertises
 * itself over mDNS so the existing Mac app's WiFi picker finds it, speaks the
 * hello/ping/pong handshake, and turns incoming wire frames into either
 * control events or [VideoFrame]s for the decoder.
 *
 * Ported from `iOS/PhoneReceiver.swift` — the Mac is a fixed peer, so message
 * shapes and constants here must match .claude/skills/wire-protocol/SKILL.md
 * exactly, not just "look similar".
 *
 * The phone LISTENS, the Mac CONNECTS — see CLAUDE.md for why.
 */
class PhoneReceiver(context: Context) {

    companion object {
        const val DEFAULT_PORT = 9000
        const val SERVICE_TYPE = "_opensidecar._tcp."
        private const val PREFS_NAME = "opendisplay"
        private const val KEY_INSTALL_ID = "installID"
        private const val KEY_SERVICE_NAME = "serviceName"
        private const val KEY_SHOW_NOTIFICATION = "showNotification"
        private const val DEFAULT_SERVICE_NAME = "OpenDisplay Android"
        private const val WATCHDOG_TIMEOUT_MS = 5_000L
        private const val PING_INTERVAL_MS = 2_000L
        private const val READ_BUFFER_SIZE = 64 * 1024
        private val ALLOWED_STORE_HOSTS = setOf("github.com", "play.google.com")

        /** The `store` field on `updateRequired` comes from an unauthenticated peer (the Mac
         * side of this socket has no auth — see SECURITY.md/SCR-001) — validated here, at the
         * wire boundary, so no future UI code has to remember to sanitize it before turning it
         * into a clickable link/intent. */
        internal fun sanitizedStoreUrl(raw: String?): String? {
            if (raw.isNullOrBlank()) return null
            val uri = try {
                java.net.URI(raw)
            } catch (e: Exception) {
                return null
            }
            if (uri.scheme != "https") return null
            if (uri.host !in ALLOWED_STORE_HOSTS) return null
            return raw
        }
    }

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val running = AtomicBoolean(false)
    private val sendLock = Any()

    /** Two listeners instead of one bound to every interface (0.0.0.0): loopback for the
     * documented USB/`adb forward` path (adbd delivers forwarded traffic to 127.0.0.1 on the
     * device), WiFi for normal discovery — narrows exposure on a multi-homed device (active
     * VPN/tethering alongside WiFi) without breaking either transport. See SECURITY.md/SCR-006. */
    private var loopbackServerSocket: ServerSocket? = null
    private var wifiServerSocket: ServerSocket? = null
    private var advertised = false

    @Volatile private var socket: Socket? = null

    @Volatile private var outputStream: OutputStream? = null

    private var loopbackAcceptJob: Job? = null
    private var wifiAcceptJob: Job? = null
    private var readJob: Job? = null
    private var pingJob: Job? = null
    private var watchdogJob: Job? = null

    @Volatile private var lastDataReceivedAt = System.currentTimeMillis()

    private val _status = MutableStateFlow(appContext.getString(R.string.status_starting))
    val status: StateFlow<String> = _status.asStateFlow()

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected.asStateFlow()

    private val _videoFrames = MutableSharedFlow<VideoFrame>(
        extraBufferCapacity = 4,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val videoFrames: SharedFlow<VideoFrame> = _videoFrames.asSharedFlow()

    private val _cursorPosition = MutableStateFlow<CursorPosition?>(null)
    val cursorPosition: StateFlow<CursorPosition?> = _cursorPosition.asStateFlow()

    private val _cursorImage = MutableSharedFlow<CursorImage>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val cursorImage: SharedFlow<CursorImage> = _cursorImage.asSharedFlow()

    private val _peerSignal = MutableStateFlow<PeerSignal?>(null)
    val peerSignal: StateFlow<PeerSignal?> = _peerSignal.asStateFlow()

    private val _perf = MutableStateFlow(PerfStats())
    val perf: StateFlow<PerfStats> = _perf.asStateFlow()
    private var framesThisWindow = 0
    private var perfWindowStartMs = System.currentTimeMillis()
    private val e2eWindow = mutableListOf<Double>()
    @Volatile private var lastRttMs: Double = 0.0

    private var nsdManager: NsdManager? = null
    private var registrationListener: NsdManager.RegistrationListener? = null
    private var multicastLock: WifiManager.MulticastLock? = null
    private var lastBoundPort: Int = DEFAULT_PORT

    /** User-editable name advertised over mDNS — a fresh install defaults to
     * the device model since `Build.MODEL` is at least somewhat identifying,
     * unlike iOS's `UIDevice.current.name` (which Apple locks down to a
     * generic "iPhone" without an entitlement personal teams can't get). */
    private val _serviceName = MutableStateFlow(loadServiceName())
    val serviceName: StateFlow<String> = _serviceName.asStateFlow()

    /** Whether [io.github.josepacelli.opendisplay.service.ReceiverService] should
     * show its status notification (connection status, version, disconnect
     * action — see #22) — user-editable in Settings (#26). Defaults on. */
    private val _showNotification = MutableStateFlow(loadShowNotification())
    val showNotification: StateFlow<Boolean> = _showNotification.asStateFlow()

    /** Clock sync (NTP-style): offset = macClock - ourClock, from the ping/pong
     * sample with the lowest RTT. Mirrors PhoneReceiver.swift exactly. */
    private data class OffsetSample(val rtt: Double, val offset: Double)
    private val offsetSamples = ArrayDeque<OffsetSample>()

    @Volatile private var clockOffsetMs: Double? = null

    var devicePixelsWide: Int = 0; private set
    var devicePixelsHigh: Int = 0; private set
    var deviceScale: Double = 1.0; private set

    val installId: String by lazy { loadOrCreateInstallId() }

    /**
     * Starts listening. Call [setPanelSize] at least once BEFORE this so the
     * first `hello` (sent the moment a Mac connects) carries real dimensions
     * instead of 0x0 — MainActivity does this during setup, before starting
     * the service/receiver.
     */
    fun start(port: Int = DEFAULT_PORT) {
        if (!running.compareAndSet(false, true)) return
        loopbackAcceptJob = scope.launch {
            // IPv4 explicitly, not InetAddress.getLoopbackAddress() (returns ::1 on this
            // hardware) — the Mac's `adb forward` override dials 127.0.0.1 specifically.
            listenLoop(port, { InetAddress.getByName("127.0.0.1") }) { loopbackServerSocket = it }
        }
        wifiAcceptJob = scope.launch {
            listenLoop(port, { NetworkInfo.localIPv4InetAddress() }) { wifiServerSocket = it }
        }
        pingJob = scope.launch { pingLoop() }
        watchdogJob = scope.launch { watchdogLoop() }
    }

    fun stop() {
        if (!running.compareAndSet(true, false)) return
        loopbackAcceptJob?.cancel()
        wifiAcceptJob?.cancel()
        readJob?.cancel()
        pingJob?.cancel()
        watchdogJob?.cancel()
        closeConnection()
        closeServerSocket(loopbackServerSocket)
        closeServerSocket(wifiServerSocket)
        loopbackServerSocket = null
        wifiServerSocket = null
        advertised = false
        unadvertise()
        _connected.value = false
        _status.value = appContext.getString(R.string.status_stopped)
    }

    private fun closeServerSocket(server: ServerSocket?) {
        try {
            server?.close()
        } catch (_: IOException) {
            // already gone
        }
    }

    /** The device locked — nobody can see the stream. Tells the Mac (so it
     * drops its virtual display instead of stranding the cursor on an
     * invisible screen) and stops listening entirely until [wake] — mirrors
     * `enterSleep` on iOS exactly, including *why* it also stops accepting
     * new connections: a wake-triggered reconnect must not rebuild the
     * display before anyone can see it again.
     *
     * The notify-then-stop sequence runs as one coroutine (not a fire-and-
     * forget [sendControl] followed immediately by [stop]'s socket close —
     * that would race the write against the close). Best-effort only: on a
     * real process kill there is no guarantee this coroutine gets to run at
     * all, same as any other "goodbye" network message on Android. */
    fun enterSleep() {
        scope.launch(Dispatchers.IO) {
            if (connected.value) sendControlBlocking(JSONObject().put("type", WireMessage.SLEEPING))
            stop()
        }
    }

    /** Screen back on — re-arm listening after [enterSleep]. */
    fun wake(port: Int = DEFAULT_PORT) {
        start(port)
    }

    /** The app is being torn down for good (task swiped away). Announced as
     * `closing`, not `sleeping`: this is deliberate, so the Mac ends the
     * session without waiting around for a wake that isn't coming. */
    fun shutDown() {
        scope.launch(Dispatchers.IO) {
            if (connected.value) sendControlBlocking(JSONObject().put("type", WireMessage.CLOSING))
            stop()
        }
    }

    /** User-initiated disconnect (the status bar notification's "Disconnect"
     * action) — same `closing` semantics as [shutDown] so the Mac doesn't
     * linger waiting for a wake, but unlike [shutDown] this keeps listening
     * and advertising, so picking this device again on the Mac reconnects
     * right away instead of needing the app relaunched. */
    fun disconnect() {
        scope.launch(Dispatchers.IO) {
            if (connected.value) sendControlBlocking(JSONObject().put("type", WireMessage.CLOSING))
            closeConnection()
        }
    }

    /** Real panel size in pixels + density, from the Activity/Compose layer.
     * Re-sends `hello` if we're already connected and the size actually
     * changed (rotation) — mirrors `setOrientation` on iOS, which rebuilds
     * the Mac's virtual display to match. */
    fun setPanelSize(widthPx: Int, heightPx: Int, density: Double) {
        val changed = widthPx != devicePixelsWide || heightPx != devicePixelsHigh
        devicePixelsWide = widthPx
        devicePixelsHigh = heightPx
        deviceScale = density
        if (changed && socket?.isConnected == true) {
            Log.info("panel size changed -> ${widthPx}x$heightPx — resending hello")
            sendHello()
        }
    }

    /** Update the mDNS-advertised name and persist it. If already advertising,
     * re-publishes immediately (NSD has no in-place rename — unregister then
     * register again) so the Mac's WiFi picker picks up the new name without
     * needing a reconnect. Blank input falls back to the default name. */
    fun setServiceName(name: String) {
        val resolved = name.trim().ifEmpty { DEFAULT_SERVICE_NAME }
        if (resolved == _serviceName.value) return
        _serviceName.value = resolved
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SERVICE_NAME, resolved)
            .apply()
        if (registrationListener != null) {
            Log.info("re-advertising as \"$resolved\"")
            unadvertise()
            advertise(lastBoundPort)
        }
    }

    /** Turn the status notification on/off and persist the choice. */
    fun setShowNotification(show: Boolean) {
        if (show == _showNotification.value) return
        _showNotification.value = show
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SHOW_NOTIFICATION, show)
            .apply()
    }

    /** Best-effort local IPv4 address for manually typing into the Mac app's
     * host/port override when mDNS discovery doesn't work (some routers and
     * corporate networks block multicast). `null` if nothing usable is found. */
    fun localAddressHint(): String? = NetworkInfo.localIPv4Address()?.let { "$it:${lastBoundPort}" }

    /** [phase]: "began" | "moved" | "ended" | "cancelled". [x]/[y] normalized
     * 0-1 against the displayed video rect (letterboxing already removed by
     * the caller). */
    fun sendTouch(phase: String, x: Double, y: Double) {
        val message = JSONObject()
            .put("type", WireMessage.TOUCH)
            .put("phase", phase)
            .put("x", x)
            .put("y", y)
        clockOffsetMs?.let { message.put("t", nowMs() + it) }
        sendControl(message)
    }

    /** [dx]/[dy] in video pixels, natural-scrolling sign. */
    fun sendScroll(dx: Double, dy: Double) {
        sendControl(
            JSONObject()
                .put("type", WireMessage.SCROLL)
                .put("dx", dx)
                .put("dy", dy),
        )
    }

    /** Ask the Mac for a fresh IDR — call when the decoder loses sync. */
    fun requestKeyframe() {
        sendControl(JSONObject().put("type", WireMessage.KEYFRAME_REQUEST))
    }

    // region Listener + connection lifecycle

    /** Shared by the loopback and WiFi listeners — [resolveBindAddress] is re-evaluated on every
     * retry so e.g. the WiFi listener starts working as soon as WiFi comes up, even if it wasn't
     * available yet when [start] was called. */
    private fun listenLoop(port: Int, resolveBindAddress: () -> InetAddress?, storeSocket: (ServerSocket) -> Unit) {
        while (running.get()) {
            val bindAddress = resolveBindAddress()
            if (bindAddress == null) {
                Thread.sleep(1000)
                continue
            }
            try {
                val server = ServerSocket()
                server.reuseAddress = true
                server.bind(InetSocketAddress(bindAddress, port))
                storeSocket(server)
                advertiseOnce(port)
                _status.value = appContext.getString(R.string.status_listening, port)
                Log.info("listening on ${bindAddress.hostAddress}:$port")
                while (running.get()) {
                    val client = server.accept()
                    Log.info("new connection from ${client.remoteSocketAddress}")
                    acceptConnection(client)
                }
            } catch (e: IOException) {
                if (!running.get()) return
                Log.warn("listener on ${bindAddress.hostAddress} failed, retrying in 1s", e)
                Thread.sleep(1000)
            }
        }
    }

    private fun advertiseOnce(port: Int) {
        if (advertised) return
        advertised = true
        advertise(port)
    }

    private fun acceptConnection(client: Socket) {
        val previousAddress = socket?.remoteSocketAddress?.toString()
        val newAddress = client.remoteSocketAddress?.toString()
        closeConnection() // replace any existing connection, like the Mac replacing its dial
        if (previousAddress != null && newAddress != null && previousAddress != newAddress) {
            Log.warn("peer changed mid-session: $previousAddress -> $newAddress")
            _peerSignal.value = PeerSignal.PeerReplaced(previousAddress, newAddress)
        }
        client.tcpNoDelay = true // touch/scroll are tiny packets; Nagle would batch them into lag
        socket = client
        outputStream = client.getOutputStream()
        lastDataReceivedAt = System.currentTimeMillis()
        _connected.value = true
        _status.value = appContext.getString(R.string.settings_status_connection_connected)
        if (devicePixelsWide == 0) {
            Log.warn("sending hello before panel size is known — caller should call setPanelSize() first")
        }
        sendHello()
        readJob = scope.launch { readLoop(client) }
    }

    private fun readLoop(client: Socket) {
        // Owned entirely by this connection's read coroutine — no sharing
        // across reconnects, so no locking needed around its mutable state.
        val frameDecoder = Framing.FrameDecoder()
        val input = client.getInputStream()
        val buf = ByteArray(READ_BUFFER_SIZE)
        try {
            while (running.get() && socket === client) {
                val n = input.read(buf)
                if (n < 0) {
                    Log.info("peer closed connection")
                    break
                }
                lastDataReceivedAt = System.currentTimeMillis()
                val frames = try {
                    frameDecoder.feed(buf, n)
                } catch (e: IllegalArgumentException) {
                    Log.error("invalid frame from peer — dropping connection", e)
                    break
                }
                for (frame in frames) dispatchFrame(frame)
            }
        } catch (e: IOException) {
            if (running.get()) Log.info("receive error: ${e.message}")
        } finally {
            if (socket === client) {
                socket = null
                outputStream = null
                _connected.value = false
                _status.value = appContext.getString(R.string.status_listening, lastBoundPort)
            }
            try {
                client.close()
            } catch (_: IOException) {
            }
        }
    }

    private fun closeConnection() {
        val client = socket ?: return
        socket = null
        outputStream = null
        _connected.value = false
        try {
            client.close()
        } catch (_: IOException) {
        }
    }

    private fun dispatchFrame(frame: ByteArray) {
        if (AnnexB.isControlJson(frame)) {
            handleControlJson(frame)
            return
        }
        val parsed = AnnexB.parse(frame)
        if (parsed.vclNalus.isEmpty() && parsed.sps == null && parsed.pps == null) return
        val telemetry = AnnexB.parseTelemetry(parsed.telemetryPrefix)
        _videoFrames.tryEmit(
            VideoFrame(parsed.sps, parsed.pps, parsed.vclNalus, telemetry.captureMs, telemetry.sendMs),
        )
        recordPerfSample(telemetry.captureMs)
    }

    /** One-second sliding window: fps + true end-to-end latency (Mac capture
     * to here), using the clock offset from [handlePong]. Simple counters,
     * not a generic metrics system — there is only ever one peer. */
    private fun recordPerfSample(captureMs: Long?) {
        framesThisWindow++
        val offset = clockOffsetMs
        if (captureMs != null && offset != null) {
            val e2e = (nowMs() + offset) - captureMs
            if (e2e > -50 && e2e < 5000) e2eWindow.add(e2e)
        }
        val elapsedMs = System.currentTimeMillis() - perfWindowStartMs
        if (elapsedMs < 1000) return

        val sorted = e2eWindow.sorted()
        _perf.value = PerfStats(
            fps = (framesThisWindow * 1000 / elapsedMs).toInt(),
            e2eP50Ms = percentile(sorted, 0.5),
            e2eP95Ms = percentile(sorted, 0.95),
            rttMs = lastRttMs,
        )
        framesThisWindow = 0
        e2eWindow.clear()
        perfWindowStartMs = System.currentTimeMillis()
    }

    private fun percentile(sorted: List<Double>, fraction: Double): Double {
        if (sorted.isEmpty()) return 0.0
        val index = (sorted.size * fraction).toInt().coerceAtMost(sorted.size - 1)
        return sorted[index]
    }

    // endregion

    // region Control messages

    private fun sendHello() {
        val message = JSONObject()
            .put("type", WireMessage.HELLO)
            .put("pixelsWide", devicePixelsWide)
            .put("pixelsHigh", devicePixelsHigh)
            .put("scale", deviceScale)
            .put("device", "Android")
            .put("id", installId)
            .put("pv", WireProtocol.VERSION)
        sendControl(message)
        Log.info("hello sent ($devicePixelsWide x $devicePixelsHigh @${deviceScale}x)")
    }

    private fun handleControlJson(payload: ByteArray) {
        val obj = try {
            JSONObject(String(payload, Charsets.UTF_8))
        } catch (e: Exception) {
            Log.warn("unparseable control message (${payload.size} bytes)", e)
            return
        }
        when (val type = obj.optString("type")) {
            WireMessage.PING -> {
                // The Mac's OWN liveness ping (separate from ours) — carries its
                // send-side health (encDrops/netDrops/pending/capFps) for its own
                // HUD equivalent. Confirmed live against the real Mac app: it
                // pings every ~2s regardless of whether we ping it. Nothing to
                // reply with — only *our* ping expects a pong back.
            }

            WireMessage.WELCOME -> {
                val macVersion = obj.optInt("pv", WireProtocol.ASSUMED_WHEN_ABSENT)
                if (macVersion < WireProtocol.MIN_SUPPORTED_PEER) {
                    _peerSignal.value = PeerSignal.UpdateMac(
                        "O OpenDisplay no seu Mac está desatualizado para este app Android. " +
                            "Atualize o OpenDisplay no Mac para reconectar.",
                    )
                }
            }

            WireMessage.UPDATE_REQUIRED -> {
                val message = obj.optString(
                    "message",
                    "Atualize o OpenDisplay para continuar usando este segundo display.",
                )
                val store = sanitizedStoreUrl(if (obj.has("store")) obj.optString("store") else null)
                _peerSignal.value = PeerSignal.UpdateAndroid(message, store)
            }

            WireMessage.PONG -> handlePong(obj)

            WireMessage.CURSOR -> {
                val visible = obj.optInt("v", 0) == 1
                _cursorPosition.value = CursorPosition(obj.optDouble("x", 0.0), obj.optDouble("y", 0.0), visible)
            }

            WireMessage.CURSOR_IMAGE -> handleCursorImage(obj)

            WireMessage.STATS -> Log.info("MAC-STATS $obj")

            else -> Log.info("unknown control message type: $type")
        }
    }

    private fun handlePong(obj: JSONObject) {
        if (!obj.has("t") || !obj.has("mt")) return
        val t1 = obj.optDouble("t", Double.NaN)
        val mt = obj.optDouble("mt", Double.NaN)
        if (t1.isNaN() || mt.isNaN()) return
        val t2 = nowMs()
        val rtt = t2 - t1
        if (rtt < 0 || rtt >= 2000) return
        lastRttMs = rtt
        val offset = mt - (t1 + t2) / 2
        offsetSamples.addLast(OffsetSample(rtt, offset))
        if (offsetSamples.size > 15) offsetSamples.removeFirst()
        clockOffsetMs = offsetSamples.minByOrNull { it.rtt }?.offset
    }

    private fun handleCursorImage(obj: JSONObject) {
        val b64 = obj.optString("png").takeIf { it.isNotEmpty() } ?: return
        val png = try {
            Base64.decode(b64, Base64.DEFAULT)
        } catch (e: IllegalArgumentException) {
            Log.warn("bad cursorImg base64", e)
            return
        }
        _cursorImage.tryEmit(
            CursorImage(
                png = png,
                normalizedWidth = obj.optDouble("nw", 0.0),
                normalizedHeight = obj.optDouble("nh", 0.0),
                anchorX = obj.optDouble("ax", 0.0),
                anchorY = obj.optDouble("ay", 0.0),
            ),
        )
    }

    private fun sendControl(json: JSONObject) {
        // Dispatched, not written inline: callers include UI-thread touch
        // handlers that must never block on a stalled socket.
        scope.launch(Dispatchers.IO) { sendControlBlocking(json) }
    }

    /** Same wire write as [sendControl], but synchronous — for the rare
     * caller (enterSleep/shutDown) that must guarantee the write happens
     * before it proceeds to tear the connection down right after. Must be
     * called from a background thread/coroutine, never the main thread. */
    private fun sendControlBlocking(json: JSONObject) {
        val out = outputStream ?: return
        val framed = Framing.encode(json.toString().toByteArray(Charsets.UTF_8))
        synchronized(sendLock) {
            try {
                out.write(framed)
                out.flush()
            } catch (e: IOException) {
                Log.info("control send error: ${e.message}")
            }
        }
    }

    // endregion

    // region Liveness

    private suspend fun pingLoop() {
        while (running.get()) {
            delay(PING_INTERVAL_MS)
            if (_connected.value) {
                sendControl(JSONObject().put("type", WireMessage.PING).put("t", nowMs()))
            }
        }
    }

    private suspend fun watchdogLoop() {
        while (running.get()) {
            delay(2000)
            if (_connected.value && System.currentTimeMillis() - lastDataReceivedAt > WATCHDOG_TIMEOUT_MS) {
                Log.info("watchdog: nothing from the Mac for >5s — dropping connection")
                closeConnection()
            }
        }
    }

    // endregion

    // region NSD (mDNS) discovery

    private fun advertise(boundPort: Int) {
        lastBoundPort = boundPort
        val manager = appContext.getSystemService(Context.NSD_SERVICE) as? NsdManager
        if (manager == null) {
            Log.warn("NsdManager unavailable — Mac WiFi discovery will not find this device")
            return
        }
        nsdManager = manager
        val info = NsdServiceInfo().apply {
            serviceName = _serviceName.value
            serviceType = SERVICE_TYPE
            port = boundPort
            setAttribute("id", installId)
            setAttribute("pv", WireProtocol.VERSION.toString())
        }
        val listener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(serviceInfo: NsdServiceInfo) {
                Log.info("NSD registered as \"${serviceInfo.serviceName}\"")
            }

            override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.warn("NSD registration failed: $errorCode")
            }

            override fun onServiceUnregistered(serviceInfo: NsdServiceInfo) {
                Log.info("NSD unregistered")
            }

            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.warn("NSD unregistration failed: $errorCode")
            }
        }
        registrationListener = listener
        try {
            manager.registerService(info, NsdManager.PROTOCOL_DNS_SD, listener)
        } catch (e: Exception) {
            Log.warn("NsdManager.registerService threw", e)
        }
        acquireMulticastLock()
    }

    private fun acquireMulticastLock() {
        val wifi = appContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager ?: return
        try {
            val lock = wifi.createMulticastLock("opendisplay-nsd")
            lock.setReferenceCounted(true)
            lock.acquire()
            multicastLock = lock
        } catch (e: Exception) {
            Log.warn("multicast lock acquisition failed", e)
        }
    }

    private fun unadvertise() {
        registrationListener?.let { listener ->
            try {
                nsdManager?.unregisterService(listener)
            } catch (e: Exception) {
                Log.warn("NSD unregisterService threw", e)
            }
        }
        registrationListener = null
        multicastLock?.let { if (it.isHeld) it.release() }
        multicastLock = null
    }

    // endregion

    private fun loadOrCreateInstallId(): String {
        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.getString(KEY_INSTALL_ID, null)?.let { return it }
        val fresh = UUID.randomUUID().toString()
        prefs.edit().putString(KEY_INSTALL_ID, fresh).apply()
        return fresh
    }

    private fun loadServiceName(): String {
        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SERVICE_NAME, null) ?: Build.MODEL ?: DEFAULT_SERVICE_NAME
    }

    private fun loadShowNotification(): Boolean {
        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_SHOW_NOTIFICATION, true)
    }

    private fun nowMs(): Double = System.currentTimeMillis().toDouble()
}
