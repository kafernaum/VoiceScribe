package com.yourdomain.voicescribe.core.audio.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.yourdomain.voicescribe.core.domain.port.ForegroundServiceController

class ForegroundServiceControllerImpl(private val context: Context) : ForegroundServiceController {

    override fun startRecording(recordingId: String) {
        val intent = Intent(context, RecordingForegroundService::class.java).apply {
            action = RecordingForegroundService.ACTION_START
            putExtra(RecordingForegroundService.EXTRA_RECORDING_ID, recordingId)
        }
        ContextCompat.startForegroundService(context, intent)
    }

    override fun updateNotification(elapsedMs: Long, isPaused: Boolean) {
        val intent = Intent(context, RecordingForegroundService::class.java).apply {
            action = RecordingForegroundService.ACTION_UPDATE
            putExtra(RecordingForegroundService.EXTRA_ELAPSED_MS, elapsedMs)
            putExtra(RecordingForegroundService.EXTRA_IS_PAUSED, isPaused)
        }
        context.startService(intent)
    }

    override fun stop() {
        val intent = Intent(context, RecordingForegroundService::class.java).apply {
            action = RecordingForegroundService.ACTION_STOP
        }
        context.startService(intent)
    }
}
