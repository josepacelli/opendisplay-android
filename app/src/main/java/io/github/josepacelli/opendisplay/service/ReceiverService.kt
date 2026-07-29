package io.github.josepacelli.opendisplay.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import io.github.josepacelli.opendisplay.MainActivity
import io.github.josepacelli.opendisplay.net.PhoneReceiver
import io.github.josepacelli.opendisplay.util.Log

/**
 * Foreground service so the TCP listener + NSD advertisement survive past
 * `MainActivity`'s lifecycle (Android can and will tear the Activity down
 * under memory pressure while the app is backgrounded — without this, the
 * Mac would see the connection just vanish rather than a deliberate
 * close/sleep).
 *
 * Owns the single [PhoneReceiver] instance for the process; `MainActivity`
 * binds to read/observe it rather than creating its own, so there is exactly
 * one session and one `hello` regardless of how many times the Activity is
 * recreated (rotation without `configChanges`, low-memory recreation, etc.).
 *
 * Screen lock/unlock is wired to [PhoneReceiver.enterSleep]/[PhoneReceiver.wake] —
 * mirrors the iOS receiver's behavior of refusing connections while nobody
 * can see the screen, rather than just letting the link silently die.
 */
class ReceiverService : Service() {

    inner class LocalBinder : Binder() {
        val receiver: PhoneReceiver get() = this@ReceiverService.receiver
    }

    private val binder = LocalBinder()
    lateinit var receiver: PhoneReceiver
        private set

    private var screenReceiverRegistered = false
    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    Log.info("screen off — entering sleep")
                    receiver.enterSleep()
                }
                Intent.ACTION_USER_PRESENT -> {
                    Log.info("screen unlocked — waking")
                    receiver.wake()
                    updateNotification()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        receiver = PhoneReceiver(applicationContext)
        startForeground(NOTIFICATION_ID, buildNotification())
        registerScreenReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        receiver.start() // idempotent — safe if already running
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    /** User swiped the app away from recents — a deliberate close, not a
     * transient background/lock, so tell the Mac with `closing` (not
     * `sleeping`) and stop the service entirely instead of lingering. */
    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.info("task removed — shutting down")
        receiver.shutDown()
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        unregisterScreenReceiverIfNeeded()
        receiver.shutDown()
        super.onDestroy()
    }

    private fun registerScreenReceiver() {
        if (screenReceiverRegistered) return
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        // NOT_EXPORTED: these are protected system broadcasts, no other app
        // needs to (or should be able to) send us a fake SCREEN_OFF/USER_PRESENT.
        ContextCompat.registerReceiver(this, screenReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        screenReceiverRegistered = true
    }

    private fun unregisterScreenReceiverIfNeeded() {
        if (!screenReceiverRegistered) return
        try {
            unregisterReceiver(screenReceiver)
        } catch (_: IllegalArgumentException) {
            // already unregistered
        }
        screenReceiverRegistered = false
    }

    private fun updateNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager?.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun buildNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                "OpenDisplay",
                NotificationManager.IMPORTANCE_LOW,
            )
            manager.createNotificationChannel(channel)
        }
        val openIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_IMMUTABLE,
        )
        // TODO(fase 9): dedicated status icon/text (fps, transport) instead
        // of this static placeholder — needs a real launcher-style small icon.
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("OpenDisplay")
            .setContentText("Waiting for your Mac to connect…")
            .setSmallIcon(android.R.drawable.presence_video_online)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "opendisplay_receiver"
    }
}
