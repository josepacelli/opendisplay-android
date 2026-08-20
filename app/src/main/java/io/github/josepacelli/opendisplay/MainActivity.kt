package io.github.josepacelli.opendisplay

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.hardware.usb.UsbAccessory
import android.hardware.usb.UsbManager
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

    /** Accessory received before the service finished binding; opened once bound. */
    private var pendingAccessory: UsbAccessory? = null

    /** Binds this Activity to [ReceiverService], surfacing its [PhoneReceiver] as [boundReceiver]. */
    private val connection = object : ServiceConnection {
        /** @param name the connected component, unused.
         * @param service the [ReceiverService.LocalBinder] from the service. */
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val receiver = (service as? ReceiverService.LocalBinder)?.receiver ?: return
            reportPanelSize(receiver)
            boundReceiver = receiver
            openPendingAccessory(receiver)
        }

        /** @param name the disconnected component, unused. */
        override fun onServiceDisconnected(name: ComponentName?) {
            boundReceiver = null
        }
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    /** The cable arrived while the app was already open (see `singleTop`).
     * @param intent the new intent, possibly carrying a USB accessory extra. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        takeAccessory(intent)
    }

    /** Stashes [intent]'s accessory extra, if any, and opens it right away if the
     * service is already bound — otherwise [onServiceConnected] picks it up later.
     * @param intent the launch or new intent to check for a USB accessory extra. */
    private fun takeAccessory(intent: Intent?) {
        val accessory = intent?.usbAccessory() ?: return
        pendingAccessory = accessory
        boundReceiver?.let { openPendingAccessory(it) }
    }

    /** Opens [pendingAccessory] (if still set) and hands its file descriptor to [receiver].
     * @param receiver the bound session to attach the accessory to. */
    private fun openPendingAccessory(receiver: PhoneReceiver) {
        val accessory = pendingAccessory ?: return
        pendingAccessory = null
        val manager = getSystemService(UsbManager::class.java)
        val descriptor = manager?.openAccessory(accessory)
        if (descriptor == null) {
            Log.warn("USB accessory vanished before it could be opened")
            return
        }
        receiver.attachAccessory(descriptor)
    }

    /** [UsbManager.EXTRA_ACCESSORY], via the type-safe overload on API 33+.
     * @return the attached [UsbAccessory], or `null` if this intent doesn't carry one. */
    private fun Intent.usbAccessory(): UsbAccessory? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(UsbManager.EXTRA_ACCESSORY, UsbAccessory::class.java)
        } else {
            @Suppress("DEPRECATION")
            getParcelableExtra(UsbManager.EXTRA_ACCESSORY)
        }

    /** Starts/binds [ReceiverService] and hosts the Compose UI.
     * @param savedInstanceState unused — this activity keeps no saved state of its own. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()

        takeAccessory(intent)

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

    /** Rotation/multi-window changes handled in-place (`android:configChanges` in the
     * manifest) — re-reports the panel size instead of recreating the Activity.
     * @param newConfig the new device configuration. */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.info("configuration changed — re-reporting panel size")
        boundReceiver?.let { reportPanelSize(it) }
    }

    /** Unbinds from [ReceiverService], if bound — the service itself keeps running. */
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
     *
     * @param receiver the session to report this Activity's current panel size to.
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
