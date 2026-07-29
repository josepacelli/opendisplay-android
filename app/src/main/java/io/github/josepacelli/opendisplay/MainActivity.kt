package io.github.josepacelli.opendisplay

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.josepacelli.opendisplay.net.PhoneReceiver
import io.github.josepacelli.opendisplay.service.ReceiverService
import io.github.josepacelli.opendisplay.ui.ReceiverScreen
import io.github.josepacelli.opendisplay.ui.theme.OpenDisplayTheme
import io.github.josepacelli.opendisplay.util.Log

/**
 * Thin UI host: the actual [PhoneReceiver] session lives in [ReceiverService]
 * (a foreground service) so it survives this Activity being recreated or
 * killed under memory pressure — this class only starts that service, binds
 * to read its [PhoneReceiver], and forwards panel-size changes to it.
 */
class MainActivity : ComponentActivity() {

    private var boundReceiver by mutableStateOf<PhoneReceiver?>(null)
    private var bound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val receiver = (service as? ReceiverService.LocalBinder)?.receiver ?: return
            reportPanelSize(receiver)
            boundReceiver = receiver
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            boundReceiver = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val serviceIntent = Intent(this, ReceiverService::class.java)
        startForegroundService(serviceIntent)
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
        bound = true

        setContent {
            OpenDisplayTheme {
                val receiver = boundReceiver
                if (receiver == null) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                        Text(
                            text = "Starting…",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                } else {
                    ReceiverScreen(receiver = receiver)
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.info("configuration changed — re-reporting panel size")
        boundReceiver?.let { reportPanelSize(it) }
    }

    override fun onDestroy() {
        if (bound) {
            unbindService(connection)
            bound = false
        }
        super.onDestroy()
    }

    /**
     * `WindowManager.currentWindowMetrics` (API 30+, verified against the
     * platform SDK — not a Jetpack dependency) reports the actual window
     * bounds, which is what matters for multi-window/split-screen on a
     * tablet; the old `DisplayMetrics` path reports the *display*, which is
     * wrong there. Falls back to `DisplayMetrics` below API 30 (minSdk 26).
     */
    private fun reportPanelSize(receiver: PhoneReceiver) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = windowManager.currentWindowMetrics
            val bounds = metrics.bounds
            receiver.setPanelSize(bounds.width(), bounds.height(), metrics.density.toDouble())
        } else {
            @Suppress("DEPRECATION")
            val metrics = resources.displayMetrics
            receiver.setPanelSize(metrics.widthPixels, metrics.heightPixels, metrics.density.toDouble())
        }
    }
}
