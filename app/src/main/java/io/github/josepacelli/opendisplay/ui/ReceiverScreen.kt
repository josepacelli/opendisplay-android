package io.github.josepacelli.opendisplay.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
            Text(
                text = status,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(24.dp),
            )
            TextButton(
                onClick = { showSettings = true },
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp),
            ) {
                Text("Configurações", color = Color.White)
            }
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
            "Conexão assumida por outro dispositivo (${signal.newAddress}). Se não foi você, verifique quem está na rede."
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
