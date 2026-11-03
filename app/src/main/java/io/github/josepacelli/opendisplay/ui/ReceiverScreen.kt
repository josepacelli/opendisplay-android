package io.github.josepacelli.opendisplay.ui

import android.provider.Settings
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.josepacelli.opendisplay.R
import io.github.josepacelli.opendisplay.net.PeerSignal
import io.github.josepacelli.opendisplay.net.PerfHudPosition
import io.github.josepacelli.opendisplay.net.PhoneReceiver
import io.github.josepacelli.opendisplay.ui.theme.LocalLampColors
import io.github.josepacelli.opendisplay.ui.theme.OverlayColors
import kotlinx.coroutines.delay

/**
 * Top-level screen: fullscreen black background, an aspect-correct video +
 * cursor box centered in it, and status/warning text overlaid only while
 * there's something the user needs to know — this app's only job is to
 * disappear behind the Mac's desktop, so the one piece of chrome once
 * connected is [ConnectedMenu], a self-hiding handle docked to a screen edge
 * (#151).
 *
 * [Modifier.safeDrawingPadding] keeps the video (and its touch-forwarding
 * surface) out from under the status bar / gesture nav area. Android 15+
 * draws every app edge-to-edge by default — without this padding, our
 * touch-capturing surface silently ate the whole screen, including the
 * system bars, so there was no way back to Android itself once connected.
 * Skipped only in immersive fullscreen (#45) — there the bars themselves are
 * hidden by [io.github.josepacelli.opendisplay.MainActivity], so the video can
 * safely use that space too.
 *
 * @param receiver the session this screen renders and forwards input to.
 */
@Composable
fun ReceiverScreen(receiver: PhoneReceiver) {
    val status by receiver.status.collectAsState()
    val connected by receiver.connected.collectAsState()
    val peerSignal by receiver.peerSignal.collectAsState()
    val connectionUnstable by receiver.connectionUnstable.collectAsState()
    val showPerfHud by receiver.showPerfHud.collectAsState()
    val perfHudPosition by receiver.perfHudPosition.collectAsState()
    val immersiveFullscreen by receiver.immersiveFullscreen.collectAsState()
    val connectedMenuEdge by receiver.connectedMenuEdge.collectAsState()
    val connectedMenuEnabled by receiver.connectedMenuEnabled.collectAsState()
    val connectedMenuIdleSeconds by receiver.connectedMenuIdleSeconds.collectAsState()
    val zoomEnabled by receiver.zoomEnabled.collectAsState()
    val cursorPosition by receiver.cursorPosition.collectAsState()
    val serviceName by receiver.serviceName.collectAsState()
    var videoDims by remember { mutableStateOf<VideoDims?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    var settingsInitialTab by remember { mutableIntStateOf(SETTINGS_TAB_GENERAL) }
    var lastActivityAt by remember { mutableLongStateOf(0L) }
    var connectedMenuVisible by remember { mutableStateOf(true) }

    LaunchedEffect(connected) {
        if (connected) {
            showSettings = false
            lastActivityAt = 0L
            connectedMenuVisible = true
        }
    }

    LaunchedEffect(lastActivityAt, connectedMenuIdleSeconds) {
        if (lastActivityAt == 0L) return@LaunchedEffect
        delay(connectedMenuIdleSeconds * 1000L)
        connectedMenuVisible = true
    }

    LaunchedEffect(cursorPosition) {
        if (connected && connectedMenuEnabled && cursorPosition != null) {
            lastActivityAt = System.currentTimeMillis()
            connectedMenuVisible = false
        }
    }

    val aspect = videoDims?.let { it.width.toFloat() / it.height.toFloat() }
        ?: run {
            val w = receiver.devicePixelsWide
            val h = receiver.devicePixelsHigh
            if (w > 0 && h > 0) w.toFloat() / h.toFloat() else 16f / 9f
        }

    val immersive = connected && immersiveFullscreen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (connected) Color.Black else MaterialTheme.colorScheme.background)
            .then(if (immersive) Modifier else Modifier.safeDrawingPadding()),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(aspect)
                .then(
                    if (connected && connectedMenuEnabled) {
                        Modifier.pointerInput(connected) {
                            while (true) {
                                awaitPointerEventScope { awaitPointerEvent(PointerEventPass.Initial) }
                                lastActivityAt = System.currentTimeMillis()
                                connectedMenuVisible = false
                            }
                        }
                    } else {
                        Modifier
                    },
                ),
        ) {
            VideoSurface(
                receiver = receiver,
                videoDims = videoDims,
                onVideoDimsChanged = { videoDims = it },
                zoomEnabled = zoomEnabled,
                modifier = Modifier.fillMaxSize(),
            )
            CursorOverlay(receiver = receiver, modifier = Modifier.fillMaxSize())
        }

        if (!connected) {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
            IdleContent(
                status = status,
                deviceName = serviceName,
                addressHint = remember(status) { receiver.localAddressHint() },
                onSettingsClick = {
                    settingsInitialTab = SETTINGS_TAB_GENERAL
                    showSettings = true
                },
                onAboutClick = {
                    settingsInitialTab = SETTINGS_TAB_ABOUT
                    showSettings = true
                },
            )
        }

        peerSignal?.let { signal ->
            if (signal is PeerSignal.PeerReplaced) {
                LaunchedEffect(signal) {
                    delay(PEER_REPLACED_BANNER_MS)
                    receiver.dismissPeerSignal(signal)
                }
            }
            PeerSignalBanner(signal, modifier = Modifier.align(Alignment.TopCenter).padding(top = 32.dp))
        }

        if (connected && showPerfHud) {
            PerfHud(receiver, modifier = Modifier.align(perfHudPosition.toAlignment()).padding(8.dp))
        }

        if (connectionUnstable) {
            ConnectionUnstableBanner(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp))
        }

        if (connected && connectedMenuEnabled && connectedMenuVisible) {
            ConnectedMenu(
                edge = connectedMenuEdge,
                onEdgeChange = receiver::setConnectedMenuEdge,
                zoomEnabled = zoomEnabled,
                onToggleZoom = receiver::setZoomEnabled,
                fullscreenEnabled = immersiveFullscreen,
                onToggleFullscreen = receiver::setImmersiveFullscreen,
                onOpenSettings = {
                    settingsInitialTab = SETTINGS_TAB_GENERAL
                    showSettings = true
                },
                onDisconnect = receiver::disconnect,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    if (showSettings) {
        SettingsDialog(receiver = receiver, initialTab = settingsInitialTab, onDismiss = { showSettings = false })
    }
}

/** How long the "taken over by another device" banner stays up (#185). It's information, not a
 * call to action, unlike the update banners, which stay until the situation changes. */
private const val PEER_REPLACED_BANNER_MS = 8_000L

/** What the idle screen's indicator lamp is saying. */
private enum class LampState { LISTENING, PROBLEM, OFF }

/** Maps the receiver's status text to a [LampState] — the receiver only exposes a localized
 * string, so this compares against the same string resources it was built from.
 * @param status the current status text.
 * @return the lamp state that status corresponds to. */
@Composable
private fun lampStateFor(status: String): LampState = when (status) {
    stringResource(R.string.status_no_wifi), stringResource(R.string.status_vpn_active) -> LampState.PROBLEM
    stringResource(R.string.status_starting), stringResource(R.string.status_stopped) -> LampState.OFF
    else -> LampState.LISTENING
}

/**
 * No-Mac-connected state, built like a piece of desk hardware: one panel, an indicator lamp
 * that carries the connection state, the device's network name at display size (it's exactly
 * what the user has to find on the Mac), the two steps to get there, and Settings/About.
 * Two columns whenever the screen is landscape and at least 600dp wide (tighter on short
 * screens like a phone on its side), one scrolling column otherwise.
 *
 * @param status human-readable listener status, shown next to the lamp.
 * @param deviceName the name this device advertises over mDNS.
 * @param addressHint this device's local `ip:port`, or `null` when there's no usable network.
 * @param onSettingsClick called when the Settings button is tapped.
 * @param onAboutClick called when the About button is tapped.
 */
@Composable
private fun IdleContent(
    status: String,
    deviceName: String,
    addressHint: String?,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
) {
    val lamp = lampStateFor(status)
    BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        val wide = maxWidth >= 600.dp && maxWidth > maxHeight
        val short = maxHeight < 480.dp
        val narrow = maxWidth < 360.dp
        val panelPadding = if (short) 24.dp else 56.dp
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.fillMaxSize(),
        ) {
            if (wide) {
                Row {
                    Column(modifier = Modifier.weight(1f).fillMaxHeight().padding(panelPadding)) {
                        Spacer(modifier = Modifier.weight(1f))
                        StatusBlock(
                            lamp,
                            status,
                            deviceName,
                            nameStyle = if (short) MaterialTheme.typography.displaySmall else MaterialTheme.typography.displayLarge,
                            gap = if (short) 16.dp else 40.dp,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        MetaBlock(addressHint)
                    }
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        Grille(modifier = Modifier.matchParentSize())
                        Column(
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize().padding(panelPadding),
                        ) {
                            Steps()
                            Spacer(modifier = Modifier.height(if (short) 16.dp else 32.dp))
                            IdleButtons(onSettingsClick, onAboutClick, modifier = Modifier.align(Alignment.End))
                        }
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
                    StatusBlock(
                        lamp,
                        status,
                        deviceName,
                        nameStyle = if (narrow) MaterialTheme.typography.displaySmall else MaterialTheme.typography.displayMedium,
                        gap = 40.dp,
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                    Steps()
                    Spacer(modifier = Modifier.height(24.dp))
                    MetaBlock(addressHint)
                    Spacer(modifier = Modifier.height(32.dp))
                    IdleButtons(onSettingsClick, onAboutClick, modifier = Modifier.align(Alignment.End))
                    Spacer(modifier = Modifier.height(32.dp))
                    Grille(modifier = Modifier.fillMaxWidth().height(96.dp))
                }
            }
        }
    }
}

/** Lamp + status line, then the device name and what it's for.
 * @param lamp the state the lamp shows.
 * @param status the status text next to the lamp.
 * @param deviceName the advertised name, set at display size.
 * @param nameStyle display role for the device name — smaller on narrow phones.
 * @param gap space between the lamp row and the name — tighter on short screens. */
@Composable
private fun StatusBlock(lamp: LampState, status: String, deviceName: String, nameStyle: TextStyle, gap: Dp) {
    val statusColor = when (lamp) {
        LampState.LISTENING -> MaterialTheme.colorScheme.tertiary
        LampState.PROBLEM -> MaterialTheme.colorScheme.error
        LampState.OFF -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        IndicatorLamp(lamp)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = status,
            color = statusColor,
            style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
    }
    Spacer(modifier = Modifier.height(gap))
    Text(
        text = deviceName,
        color = MaterialTheme.colorScheme.onSurface,
        style = nameStyle,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = stringResource(R.string.idle_name_hint),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyLarge,
    )
}

/** The idle screen's signature: a lamp whose colour is the connection state. While listening, a
 * ring slowly breathes out of it — skipped when the system's animations are turned off.
 * @param lamp the state to show. */
@Composable
private fun IndicatorLamp(lamp: LampState) {
    val color = when (lamp) {
        LampState.LISTENING -> MaterialTheme.colorScheme.tertiary
        LampState.PROBLEM -> MaterialTheme.colorScheme.error
        LampState.OFF -> LocalLampColors.current.off
    }
    val context = LocalContext.current
    val animate = lamp == LampState.LISTENING && remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
    }
    val pulse = if (animate) {
        rememberInfiniteTransition(label = "lamp").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(2200, easing = LinearOutSlowInEasing)),
            label = "lamp-pulse",
        ).value
    } else {
        0f
    }
    Canvas(modifier = Modifier.size(64.dp)) {
        val core = 12.dp.toPx()
        drawCircle(color.copy(alpha = 0.10f), radius = core + 18.dp.toPx())
        drawCircle(color.copy(alpha = 0.18f), radius = core + 8.dp.toPx())
        if (animate) {
            drawCircle(
                color.copy(alpha = 0.45f * (1f - pulse)),
                radius = core + 20.dp.toPx() * pulse,
                style = Stroke(width = 2.dp.toPx()),
            )
        }
        drawCircle(color, radius = core)
    }
}

/** The two steps that get the Mac connected, as one grouped surface.
 * Uses the existing instruction strings so every locale already has them. */
@Composable
private fun Steps() {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            StepRow(1, stringResource(R.string.idle_instruction_wifi))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(start = 76.dp))
            StepRow(2, stringResource(R.string.idle_instruction_keep_open))
        }
    }
}

/** One numbered step.
 * @param number the step's position.
 * @param text what to do. */
@Composable
private fun StepRow(number: Int, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
        ) {
            Text(
                text = number.toString(),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleSmall.copy(fontFeatureSettings = "tnum"),
            )
        }
        Spacer(modifier = Modifier.width(20.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

/** Network address and version, in tabular figures so the numbers line up.
 * @param addressHint this device's `ip:port`, or `null`. */
@Composable
private fun MetaBlock(addressHint: String?) {
    val context = LocalContext.current
    val versionName = remember {
        @Suppress("DEPRECATION")
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull()
    }
    val numbers = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_wifi),
                contentDescription = stringResource(R.string.settings_section_network),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = addressHint ?: "—", style = numbers, color = MaterialTheme.colorScheme.onSurface)
        }
        Text(
            text = versionName?.let { stringResource(R.string.about_version, it) } ?: stringResource(R.string.about_version_unknown),
            style = numbers,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Settings (the one primary action) and About.
 * @param onSettingsClick called when Settings is tapped.
 * @param onAboutClick called when About is tapped.
 * @param modifier applied to the button row. */
@Composable
private fun IdleButtons(onSettingsClick: () -> Unit, onAboutClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = modifier) {
        FilledTonalButton(onClick = onAboutClick, modifier = Modifier.heightIn(min = 52.dp)) {
            Icon(painterResource(R.drawable.ic_info), contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.settings_section_about))
        }
        Button(onClick = onSettingsClick, modifier = Modifier.heightIn(min = 52.dp)) {
            Icon(painterResource(R.drawable.ic_settings), contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.settings_title))
        }
    }
}

/** The panel's one material: a perforated speaker-grille dot field.
 * @param modifier sets the area the dots fill. */
@Composable
private fun Grille(modifier: Modifier = Modifier) {
    val dot = MaterialTheme.colorScheme.outlineVariant
    Canvas(modifier = modifier) {
        val step = 16.dp.toPx()
        val radius = 2.dp.toPx()
        var y = step / 2
        while (y < size.height) {
            var x = step / 2
            while (x < size.width) {
                drawCircle(dot, radius = radius, center = Offset(x, y))
                x += step
            }
            y += step
        }
    }
}

/** @return the screen corner this position corresponds to. */
private fun PerfHudPosition.toAlignment(): Alignment = when (this) {
    PerfHudPosition.TOP_START -> Alignment.TopStart
    PerfHudPosition.TOP_END -> Alignment.TopEnd
    PerfHudPosition.BOTTOM_START -> Alignment.BottomStart
    PerfHudPosition.BOTTOM_END -> Alignment.BottomEnd
}

/** Small readout, parity with the iOS receiver's perf overlay — fps /
 * end-to-end latency / control-channel round trip. Optional, toggled in
 * Settings (#36). Tabular figures so the line doesn't jitter as numbers change.
 * @param receiver source of the live [io.github.josepacelli.opendisplay.net.PerfStats].
 * @param modifier applied to the readout's background/padding. */
@Composable
private fun PerfHud(receiver: PhoneReceiver, modifier: Modifier = Modifier) {
    val perf by receiver.perf.collectAsState()
    Text(
        text = "${perf.fps} fps · e2e ${perf.e2eP50Ms.toInt()}/${perf.e2eP95Ms.toInt()}ms · rtt ${perf.rttMs.toInt()}ms",
        color = OverlayColors.ink,
        style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
        modifier = modifier
            .background(OverlayColors.strip, MaterialTheme.shapes.small)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

/** Banner for whatever [PeerSignal] the Mac last sent — update warnings or a
 * peer-replaced notice.
 * @param signal the signal to render.
 * @param modifier applied to the banner surface. */
@Composable
private fun PeerSignalBanner(signal: PeerSignal, modifier: Modifier = Modifier) {
    val message = when (signal) {
        is PeerSignal.UpdateMac -> signal.message
        is PeerSignal.UpdateAndroid -> signal.message
        is PeerSignal.PeerReplaced ->
            stringResource(R.string.peer_replaced_warning, signal.newAddress)
    }
    Surface(
        modifier = modifier.padding(horizontal = 16.dp).widthIn(max = 640.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.large,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            Icon(painterResource(R.drawable.ic_info), contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** Transient pill shown while [PhoneReceiver.connectionUnstable] is true — the video
 * pipeline just had to resync (#61), most often a flaky WiFi connection to the Mac.
 * @param modifier applied to the pill surface. */
@Composable
private fun ConnectionUnstableBanner(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.padding(horizontal = 16.dp),
        color = OverlayColors.strip,
        contentColor = OverlayColors.ink,
        shape = CircleShape,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
            Box(modifier = Modifier.size(10.dp).background(OverlayColors.danger, CircleShape))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = stringResource(R.string.connection_unstable_warning), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
