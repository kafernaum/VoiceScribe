package com.yourdomain.voicescribe.core.domain.usecase

import com.yourdomain.voicescribe.core.common.AppError
import com.yourdomain.voicescribe.core.common.AppResult
import com.yourdomain.voicescribe.core.domain.port.AudioCaptureController
import com.yourdomain.voicescribe.core.domain.port.ForegroundServiceController
import com.yourdomain.voicescribe.core.domain.repository.RecordingRepository
import kotlinx.coroutines.flow.first

class StopRecordingUseCase(
    private val audioCaptureController: AudioCaptureController,
    private val foregroundServiceController: ForegroundServiceController,
    private val recordingRepository: RecordingRepository,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    suspend operator fun invoke(recordingId: String): AppResult<Unit> {
        audioCaptureController.stop()
        foregroundServiceController.stop()

        val current = recordingRepository.observeRecording(recordingId).first()
            ?: return AppResult.Failure(AppError.Unknown(IllegalStateException("Recording $recordingId not found")))

        val updated = current.copy(durationMs = clock() - current.createdAtEpochMs)
        return recordingRepository.update(updated)
    }
}
