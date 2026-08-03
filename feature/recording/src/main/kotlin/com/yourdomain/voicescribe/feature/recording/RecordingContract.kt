package com.yourdomain.voicescribe.feature.recording

import com.yourdomain.voicescribe.core.domain.model.RecordingConfig
import com.yourdomain.voicescribe.core.domain.model.TranscriptSegment

/** User/system intents the recording screen can send — the "I" in MVI. */
sealed interface RecordingIntent {
    data class StartRecording(val config: RecordingConfig) : RecordingIntent
    data object PauseOrResume : RecordingIntent
    data object Stop : RecordingIntent
    data class AddBookmark(val label: String? = null) : RecordingIntent
    data object Close : RecordingIntent
    data object DismissError : RecordingIntent
}

/** Single immutable view state — the "M" in MVI. */
data class RecordingUiState(
    val phase: Phase = Phase.IDLE,
    val recordingId: String? = null,
    val elapsedMs: Long = 0L,
    val audioLevel: Float = 0f,
    val partialText: String = "",
    val segments: List<TranscriptSegment> = emptyList(),
    val isPaused: Boolean = false,
    val config: RecordingConfig = RecordingConfig(),
    val errorMessage: String? = null,
) {
    enum class Phase { IDLE, PREPARING, RECORDING, STOPPED, ERROR }

    val wordsPerMinute: Int
        get() {
            if (elapsedMs <= 0L) return 0
            val minutes = elapsedMs / 60_000.0
            val words = segments.filter { it.isFinal }.sumOf { it.text.trim().split(Regex("\\s+")).size }
            return if (minutes > 0) (words / minutes).toInt() else 0
        }

    val averageConfidence: Float?
        get() {
            val confidences = segments.mapNotNull { it.confidence }
            return if (confidences.isEmpty()) null else confidences.average().toFloat()
        }
}

/** One-off, non-state side effects — navigation, snackbars. */
sealed interface RecordingEffect {
    data object NavigateToLibrary : RecordingEffect
    data class ShowMessage(val message: String) : RecordingEffect
}
