package io.github.josepacelli.opendisplay

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
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

    // No-op result handler: if denied, the foreground service still runs fine —
    // only its persistent notification (status/version/disconnect, see #22)
    // silently won't show, same as before this permission request existed.
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()

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
                            text = stringResource(R.string.status_starting),
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                } else {
                    // Mirrors iOS's `isIdleTimerDisabled = true`: this app IS the
                    // screen while a Mac is actually mirroring, so the system's
                    // inactivity timeout must not fire mid-session (issue #10).
                    // Only while connected, though — idle/waiting should time out
                    // normally like any other app (issue #28). Doesn't block a
                    // manual power-button lock either way — screen lock/unlock
                    // still goes through PhoneReceiver.enterSleep()/wake() via
                    // ReceiverService's SCREEN_OFF/SCREEN_ON receiver.
                    LaunchedEffect(receiver) {
                        receiver.connected.collect { connected ->
                            if (connected) {
                                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                            } else {
                                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                            }
                        }
                    }
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

    /** POST_NOTIFICATIONS is a runtime permission from API 33 (Tiramisu) on —
     * without asking, a fresh install defaults it to denied and the
     * foreground service's status notification (#22) just silently never
     * shows, with no indication to the user anything's missing (issue #24). */
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    /**
     * `WindowManager.currentWindowMetrics` (API 30+, verified against the
     * platform SDK — not a Jetpack dependency) reports the actual window
     * bounds, which is what matters for multi-window/split-screen on a
     * tablet; the old `DisplayMetrics` path reports the *display*, which is
     * wrong there. Falls back to `DisplayMetrics` below API 30 (minSdk 26).
     */
    private fun reportPanelSize(receiver: PhoneReceiver) {
        val density = resources.displayMetrics.density.toDouble()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = windowManager.currentWindowMetrics
            val bounds = metrics.bounds
            receiver.setPanelSize(bounds.width(), bounds.height(), density)
        } else {
            @Suppress("DEPRECATION")
            val metrics = resources.displayMetrics
            receiver.setPanelSize(metrics.widthPixels, metrics.heightPixels, density)
        }
    }
}
