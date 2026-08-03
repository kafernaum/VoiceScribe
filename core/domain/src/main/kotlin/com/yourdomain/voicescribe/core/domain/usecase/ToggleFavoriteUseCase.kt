package com.yourdomain.voicescribe.core.domain.usecase

import com.yourdomain.voicescribe.core.common.AppResult
import com.yourdomain.voicescribe.core.domain.repository.RecordingRepository

class ToggleFavoriteUseCase(private val recordingRepository: RecordingRepository) {
    suspend operator fun invoke(recordingId: String, isFavorite: Boolean): AppResult<Unit> =
        recordingRepository.setFavorite(recordingId, isFavorite)
}
