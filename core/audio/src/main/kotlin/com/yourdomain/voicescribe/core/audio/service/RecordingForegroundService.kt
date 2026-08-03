package com.yourdomain.voicescribe.core.audio.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.yourdomain.voicescribe.core.audio.R
import com.yourdomain.voicescribe.core.common.Constants
import com.yourdomain.voicescribe.core.common.extensions.toTimestamp

/**
 * The `microphone`-typed foreground service required by Android 14+ to keep
 * recording alive while the app is backgrounded. Driven entirely by Intent
 * actions from [ForegroundServiceControllerImpl] rather than binding, to keep
 * the use-case layer free of Android `Service` lifecycle concerns.
 */
class RecordingForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startAsForeground()
            ACTION_UPDATE -> {
                val elapsedMs = intent.getLongExtra(EXTRA_ELAPSED_MS, 0L)
                val isPaused = intent.getBooleanExtra(EXTRA_IS_PAUSED, false)
                notify(elapsedMs, isPaused)
            }
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startAsForeground() {
        ensureNotificationChannel()
        val notification = buildNotification(elapsedMs = 0L, isPaused = false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(Constants.NOTIFICATION_ID_RECORDING, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(Constants.NOTIFICATION_ID_RECORDING, notification)
        }
    }

    private fun notify(elapsedMs: Long, isPaused: Boolean) {
        val manager = getSystemService(NotificationManager::class.java)
        manager?.notify(Constants.NOTIFICATION_ID_RECORDING, buildNotification(elapsedMs, isPaused))
    }

    private fun buildNotification(elapsedMs: Long, isPaused: Boolean): Notification {
        val statusText = if (isPaused) {
            getString(R.string.notification_recording_paused)
        } else {
            elapsedMs.toTimestamp(includeMillis = false)
        }
        return NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID_RECORDING)
            .setContentTitle(getString(R.string.notification_recording_title))
            .setContentText(statusText)
            .setSmallIcon(R.drawable.ic_notification_mic)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(Constants.NOTIFICATION_CHANNEL_ID_RECORDING) != null) return
        manager.createNotificationChannel(
            NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID_RECORDING,
                getString(R.string.notification_channel_recording),
                NotificationManager.IMPORTANCE_LOW,
            ),
        )
    }

    companion object {
        const val ACTION_START = "com.yourdomain.voicescribe.action.START_RECORDING"
        const val ACTION_UPDATE = "com.yourdomain.voicescribe.action.UPDATE_RECORDING"
        const val ACTION_STOP = "com.yourdomain.voicescribe.action.STOP_RECORDING"
        const val EXTRA_RECORDING_ID = "extra_recording_id"
        const val EXTRA_ELAPSED_MS = "extra_elapsed_ms"
        const val EXTRA_IS_PAUSED = "extra_is_paused"
    }
}
