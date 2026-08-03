package com.yourdomain.voicescribe.core.domain.usecase

import com.yourdomain.voicescribe.core.common.AppResult
import com.yourdomain.voicescribe.core.domain.model.Bookmark
import com.yourdomain.voicescribe.core.domain.repository.RecordingRepository
import java.util.UUID

class AddBookmarkUseCase(
    private val recordingRepository: RecordingRepository,
    private val idGenerator: () -> String = { UUID.randomUUID().toString() },
) {
    suspend operator fun invoke(
        recordingId: String,
        positionMs: Long,
        label: String? = null,
        isAutoDetected: Boolean = false,
    ): AppResult<Unit> {
        val bookmark = Bookmark(
            id = idGenerator(),
            recordingId = recordingId,
            positionMs = positionMs,
            label = label,
            isAutoDetected = isAutoDetected,
        )
        return recordingRepository.addBookmark(bookmark)
    }
}
