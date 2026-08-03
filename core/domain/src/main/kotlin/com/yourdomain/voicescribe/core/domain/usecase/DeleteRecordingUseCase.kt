package com.yourdomain.voicescribe.core.domain.usecase

import com.yourdomain.voicescribe.core.common.AppResult
import com.yourdomain.voicescribe.core.domain.repository.RecordingRepository

class DeleteRecordingUseCase(private val recordingRepository: RecordingRepository) {
    suspend fun moveToTrash(recordingId: String, nowEpochMs: Long): AppResult<Unit> =
        recordingRepository.moveToTrash(recordingId, nowEpochMs)

    suspend fun restore(recordingId: String): AppResult<Unit> =
        recordingRepository.restoreFromTrash(recordingId)

    suspend fun deletePermanently(recordingId: String): AppResult<Unit> =
        recordingRepository.deletePermanently(recordingId)

    suspend fun purgeExpired(retentionDays: Int, nowEpochMs: Long): AppResult<Int> {
        val cutoff = nowEpochMs - retentionDays * 24L * 60L * 60L * 1000L
        return recordingRepository.purgeTrashOlderThan(cutoff)
    }
}
