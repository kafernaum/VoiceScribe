package com.yourdomain.voicescribe.core.domain.usecase

import com.yourdomain.voicescribe.core.domain.port.AudioCaptureController
import com.yourdomain.voicescribe.core.domain.port.ForegroundServiceController

class PauseResumeRecordingUseCase(
    private val audioCaptureController: AudioCaptureController,
    private val foregroundServiceController: ForegroundServiceController,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private var pausedAtMs: Long? = null

    fun pause() {
        audioCaptureController.pause()
        pausedAtMs = clock()
        foregroundServiceController.updateNotification(elapsedMs = pausedAtMs ?: 0L, isPaused = true)
    }

    fun resume() {
        audioCaptureController.resume()
        pausedAtMs = null
    }

    val isPaused: Boolean get() = pausedAtMs != null
}
