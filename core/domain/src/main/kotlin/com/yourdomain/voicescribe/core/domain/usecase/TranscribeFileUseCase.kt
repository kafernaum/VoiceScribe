package com.yourdomain.voicescribe.core.domain.usecase

import com.yourdomain.voicescribe.core.common.AppResult
import com.yourdomain.voicescribe.core.domain.model.SttEngine
import com.yourdomain.voicescribe.core.domain.port.SpeechRecognizerProvider
import com.yourdomain.voicescribe.core.domain.repository.RecordingRepository

/** Batch/offline transcription for [com.yourdomain.voicescribe.core.domain.model.RecordingMode.FILE] recordings. */
class TranscribeFileUseCase(
    private val speechRecognizerProvider: SpeechRecognizerProvider,
    private val recordingRepository: RecordingRepository,
) {
    suspend operator fun invoke(
        recordingId: String,
        filePath: String,
        locale: String,
        engine: SttEngine,
    ): AppResult<Unit> {
        val session = speechRecognizerProvider.getSession(locale, engine)
        return when (val result = session.transcribeFile(filePath)) {
            is AppResult.Success -> recordingRepository.replaceSegments(recordingId, result.data)
            is AppResult.Failure -> result
        }
    }
}
