package com.yourdomain.voicescribe.core.domain.port

/**
 * Port over starting/stopping the `microphone`-typed foreground service that
 * keeps recording alive in the background (Android 14+ requires the
 * `FOREGROUND_SERVICE_TYPE_MICROPHONE` declaration — see AndroidManifest.xml).
 */
interface ForegroundServiceController {
    fun startRecording(recordingId: String)
    fun updateNotification(elapsedMs: Long, isPaused: Boolean)
    fun stop()
}
