package com.yourdomain.voicescribe.core.domain.usecase

import com.yourdomain.voicescribe.core.domain.model.Recording
import com.yourdomain.voicescribe.core.domain.repository.RecordingRepository
import kotlinx.coroutines.flow.Flow

/** Single-recording, fully-hydrated (bookmarks + transcript) stream — used by the player/detail screens. */
class ObserveRecordingUseCase(private val recordingRepository: RecordingRepository) {
    operator fun invoke(recordingId: String): Flow<Recording?> = recordingRepository.observeRecording(recordingId)
}
