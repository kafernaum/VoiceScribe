package com.yourdomain.voicescribe.core.domain.usecase

import com.yourdomain.voicescribe.core.common.AppError
import com.yourdomain.voicescribe.core.common.AppResult
import com.yourdomain.voicescribe.core.domain.model.ExportFormat
import com.yourdomain.voicescribe.core.domain.port.ExportWriter
import com.yourdomain.voicescribe.core.domain.repository.RecordingRepository
import kotlinx.coroutines.flow.first

class ExportRecordingUseCase(
    private val recordingRepository: RecordingRepository,
    private val exportWriter: ExportWriter,
) {
    suspend operator fun invoke(recordingId: String, format: ExportFormat, destinationUri: String): AppResult<String> {
        val recording = recordingRepository.observeRecording(recordingId).first()
            ?: return AppResult.Failure(AppError.Unknown(IllegalStateException("Recording $recordingId not found")))
        return exportWriter.export(recording, format, destinationUri)
    }
}
