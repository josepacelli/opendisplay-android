package io.github.josepacelli.opendisplay.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.josepacelli.opendisplay.R
import io.github.josepacelli.opendisplay.net.PeerSignal
import io.github.josepacelli.opendisplay.net.PhoneReceiver

/**
 * Top-level screen: fullscreen black background, an aspect-correct video +
 * cursor box centered in it, and status/warning text overlaid only while
 * there's something the user needs to know (no chrome once video is
 * flowing — this app's only job is to disappear behind the Mac's desktop).
 *
 * [Modifier.safeDrawingPadding] keeps the video (and its touch-forwarding
 * surface) out from under the status bar / gesture nav area. Android 15+
 * draws every app edge-to-edge by default — without this padding, our
 * touch-capturing surface silently ate the whole screen, including the
 * system bars, so there was no way back to Android itself once connected.
 */
@Composable
fun ReceiverScreen(receiver: PhoneReceiver) {
    val status by receiver.status.collectAsState()
    val connected by receiver.connected.collectAsState()
    val peerSignal by receiver.peerSignal.collectAsState()
    var videoDims by remember { mutableStateOf<VideoDims?>(null) }
    var showSettings by remember { mutableStateOf(false) }

    // The dialog only makes sense while disconnected (see SettingsDialog's
    // doc comment) — if the Mac connects while it happens to be open, the
    // now-actively-rendering VideoSurface behind it swallows further mouse
    // input meant for the dialog, leaving it stuck open. Closing it the
    // moment we connect sidesteps that instead of relying on it staying
    // dismissible on top of live video.
    LaunchedEffect(connected) {
        if (connected) showSettings = false
    }

    val aspect = videoDims?.let { it.width.toFloat() / it.height.toFloat() }
        ?: run {
            val w = receiver.devicePixelsWide
            val h = receiver.devicePixelsHigh
            if (w > 0 && h > 0) w.toFloat() / h.toFloat() else 16f / 9f
        }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .safeDrawingPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Box(modifier = Modifier.aspectRatio(aspect)) {
            VideoSurface(
                receiver = receiver,
                videoDims = videoDims,
                onVideoDimsChanged = { videoDims = it },
                modifier = Modifier.fillMaxSize(),
            )
            CursorOverlay(receiver = receiver, modifier = Modifier.fillMaxSize())
        }

        if (!connected) {
            // Scrim over the video box — otherwise the last decoded frame
            // stays visible behind the status text after a disconnect.
            Box(modifier = Modifier.fillMaxSize().background(Color.Black))
            IdleContent(status = status, onSettingsClick = { showSettings = true })
        }

        peerSignal?.let { signal ->
            PeerSignalBanner(signal, modifier = Modifier.align(Alignment.TopCenter).padding(top = 32.dp))
        }

        if (connected) {
            PerfHud(receiver, modifier = Modifier.align(Alignment.TopStart).padding(8.dp))
        }
    }

    if (showSettings) {
        SettingsDialog(receiver = receiver, onDismiss = { showSettings = false })
    }
}

/**
 * No-Mac-connected state — mirrors the upstream iOS client's `IdleView`:
 * logo, title, a colored status dot, a short instructions card and a
 * proper Settings button, instead of a bare status line.
 */
@Composable
private fun IdleContent(status: String, onSettingsClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.widthIn(max = 420.dp).padding(24.dp),
    ) {
        // The app icon is an adaptive icon (mipmap XML with separate
        // background/foreground layers) — painterResource can't load that
        // directly, so recreate the round launcher look from its layers.
        Box(
            modifier = Modifier.size(96.dp).clip(CircleShape).background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.app_name),
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            // This content only renders while disconnected — the dot mirrors
            // the iOS IdleView's semantics (green = connected), so it's
            // orange here by construction, not a value read from `status`.
            Box(modifier = Modifier.size(8.dp).background(Color(0xFFFFA000), CircleShape))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = status, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Surface(
            color = Color.White.copy(alpha = 0.08f),
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                InstructionRow(stringResource(R.string.idle_instruction_wifi))
                Spacer(modifier = Modifier.height(14.dp))
                InstructionRow(stringResource(R.string.idle_instruction_keep_open))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(onClick = onSettingsClick) {
            Text(stringResource(R.string.settings_title))
        }
    }
}

@Composable
private fun InstructionRow(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(text = "•", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodySmall)
    }
}

/** Small always-on readout, parity with the iOS receiver's perf overlay —
 * fps / end-to-end latency / control-channel round trip. */
@Composable
private fun PerfHud(receiver: PhoneReceiver, modifier: Modifier = Modifier) {
    val perf by receiver.perf.collectAsState()
    Text(
        text = "${perf.fps} fps · e2e ${perf.e2eP50Ms.toInt()}/${perf.e2eP95Ms.toInt()}ms · rtt ${perf.rttMs.toInt()}ms",
        color = Color.White,
        style = MaterialTheme.typography.labelSmall,
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.5f), MaterialTheme.shapes.small)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

@Composable
private fun PeerSignalBanner(signal: PeerSignal, modifier: Modifier = Modifier) {
    val message = when (signal) {
        is PeerSignal.UpdateMac -> signal.message
        is PeerSignal.UpdateAndroid -> signal.message
        is PeerSignal.PeerReplaced ->
            stringResource(R.string.peer_replaced_warning, signal.newAddress)
    }
    Surface(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        color = Color(0xFFB00020),
        shape = MaterialTheme.shapes.medium,
    ) {
        Text(
            text = message,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp),
        )
    }
}
