package com.yourdomain.voicescribe.core.domain.usecase

import com.yourdomain.voicescribe.core.common.AppError
import com.yourdomain.voicescribe.core.common.Constants
import com.yourdomain.voicescribe.core.domain.model.Recording
import com.yourdomain.voicescribe.core.domain.model.RecordingConfig
import com.yourdomain.voicescribe.core.domain.model.RecordingSessionState
import com.yourdomain.voicescribe.core.domain.model.TranscriptSegment
import com.yourdomain.voicescribe.core.domain.port.AudioCaptureController
import com.yourdomain.voicescribe.core.domain.port.ForegroundServiceController
import com.yourdomain.voicescribe.core.domain.port.SpeechRecognizerProvider
import com.yourdomain.voicescribe.core.domain.port.VadEvent
import com.yourdomain.voicescribe.core.domain.port.VadProcessor
import com.yourdomain.voicescribe.core.domain.repository.RecordingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import java.util.UUID

/**
 * Orchestrates one streaming recording session end-to-end: prepares the STT
 * model, opens the foreground service + microphone, routes PCM through VAD
 * into the recognizer, and persists transcript segments incrementally.
 *
 * See ARCHITECTURE.md for the full pipeline diagram and
 * docs/adrs/0002-audiorecord-not-mediarecorder-for-stt.md for why streaming
 * uses AudioRecord rather than MediaRecorder.
 */
class StartRecordingUseCase(
    private val audioCaptureController: AudioCaptureController,
    private val speechRecognizerProvider: SpeechRecognizerProvider,
    private val vadProcessor: VadProcessor,
    private val recordingRepository: RecordingRepository,
    private val foregroundServiceController: ForegroundServiceController,
    private val idGenerator: () -> String = { UUID.randomUUID().toString() },
    private val clock: () -> Long = System::currentTimeMillis,
) {
    operator fun invoke(config: RecordingConfig): Flow<RecordingSessionState> = flow {
        emit(RecordingSessionState.PreparingModel)

        val session = speechRecognizerProvider.getSession(config.locale, config.engine)
        session.ensureModelDownloaded().collect { /* progress could be surfaced as its own state if the UI needs a progress bar */ }

        val recordingId = idGenerator()
        val startedAt = clock()
        recordingRepository.insert(Recording.createNew(config, recordingId, startedAt))
        foregroundServiceController.startRecording(recordingId)

        var currentLevel = 0f
        val pcmForStt: Flow<ShortArray> = audioCaptureController.start(config.quality).mapNotNull { chunk ->
            currentLevel = chunk.rmsLevel
            val isSilence = config.vadEnabled && vadProcessor.classify(chunk) == VadEvent.SILENCE
            if (isSilence) null else chunk.samples
        }

        val segments = mutableListOf<TranscriptSegment>()
        var lastPersistMs = 0L

        session.transcribeStreaming(pcmForStt).collect { segment ->
            val existingIndex = segments.indexOfFirst { it.id == segment.id }
            if (existingIndex >= 0) segments[existingIndex] = segment else segments += segment

            val now = clock()
            if (segment.isFinal || now - lastPersistMs > Constants.TRANSCRIPT_PERSIST_THROTTLE_MS) {
                recordingRepository.appendOrUpdateSegment(recordingId, segment)
                lastPersistMs = now
            }

            emit(
                RecordingSessionState.Recording(
                    recordingId = recordingId,
                    elapsedMs = now - startedAt,
                    audioLevel = currentLevel,
                    partialText = segment.text,
                    segments = segments.toList(),
                    isPaused = false,
                ),
            )
        }
    }.catch { throwable ->
        foregroundServiceController.stop()
        emit(RecordingSessionState.Error(null, AppError.Unknown(throwable)))
    }.flowOn(Dispatchers.IO)
}
