package com.yourdomain.voicescribe.core.domain.model

/**
 * Business-level state emitted by [com.yourdomain.voicescribe.core.domain.usecase.StartRecordingUseCase].
 * The presentation layer (feature:recording's MVI `RecordingUiState`) maps
 * this into view state; it deliberately does not carry any Android types.
 */
sealed interface RecordingSessionState {
    data object Idle : RecordingSessionState
    data object PreparingModel : RecordingSessionState
    data class Recording(
        val recordingId: String,
        val elapsedMs: Long,
        val audioLevel: Float,
        val partialText: String,
        val segments: List<TranscriptSegment>,
        val isPaused: Boolean,
    ) : RecordingSessionState

    data class Stopped(val recordingId: String) : RecordingSessionState
    data class Error(val recordingId: String?, val error: com.yourdomain.voicescribe.core.common.AppError) : RecordingSessionState
}
