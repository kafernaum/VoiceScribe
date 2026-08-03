package com.yourdomain.voicescribe.core.domain.usecase

import app.cash.turbine.test
import com.yourdomain.voicescribe.core.common.AppResult
import com.yourdomain.voicescribe.core.domain.model.AudioQuality
import com.yourdomain.voicescribe.core.domain.model.Bookmark
import com.yourdomain.voicescribe.core.domain.model.LibraryFilter
import com.yourdomain.voicescribe.core.domain.model.Recording
import com.yourdomain.voicescribe.core.domain.model.RecordingConfig
import com.yourdomain.voicescribe.core.domain.model.RecordingSessionState
import com.yourdomain.voicescribe.core.domain.model.SttEngine
import com.yourdomain.voicescribe.core.domain.model.TranscriptSegment
import com.yourdomain.voicescribe.core.domain.port.AudioCaptureController
import com.yourdomain.voicescribe.core.domain.port.ForegroundServiceController
import com.yourdomain.voicescribe.core.domain.port.PcmChunk
import com.yourdomain.voicescribe.core.domain.port.SpeechRecognizerProvider
import com.yourdomain.voicescribe.core.domain.port.SpeechRecognizerSession
import com.yourdomain.voicescribe.core.domain.port.VadEvent
import com.yourdomain.voicescribe.core.domain.port.VadProcessor
import com.yourdomain.voicescribe.core.domain.repository.RecordingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class StartRecordingUseCaseTest {

    private val fakeAudioCaptureController = object : AudioCaptureController {
        override val isCapturing: Boolean = true
        override fun start(quality: AudioQuality): Flow<PcmChunk> =
            flowOf(PcmChunk(samples = shortArrayOf(1_000, -1_000), rmsLevel = 0.6f, timestampMs = 0L))
        override fun pause() = Unit
        override fun resume() = Unit
        override suspend fun stop() = Unit
    }

    private val fakeVadProcessor = object : VadProcessor {
        override fun classify(chunk: PcmChunk): VadEvent = VadEvent.SPEECH_START
        override fun reset() = Unit
    }

    private val fakeSession = object : SpeechRecognizerSession {
        override val engine: SttEngine = SttEngine.SYSTEM_ON_DEVICE
        override fun ensureModelDownloaded(): Flow<Float> = flowOf(1f)
        override fun transcribeStreaming(pcm: Flow<ShortArray>): Flow<TranscriptSegment> = flowOf(
            TranscriptSegment(id = "segment-0", startMs = 0, endMs = 500, text = "hello world", confidence = 0.9f, isFinal = true),
        )
        override suspend fun transcribeFile(filePath: String): AppResult<List<TranscriptSegment>> = AppResult.Success(emptyList())
        override fun release() = Unit
    }

    private val fakeSpeechRecognizerProvider = object : SpeechRecognizerProvider {
        override fun getSession(locale: String, engine: SttEngine): SpeechRecognizerSession = fakeSession
    }

    private class FakeForegroundServiceController : ForegroundServiceController {
        var started = false
            private set
        var stopped = false
            private set

        override fun startRecording(recordingId: String) { started = true }
        override fun updateNotification(elapsedMs: Long, isPaused: Boolean) = Unit
        override fun stop() { stopped = true }
    }

    private class FakeRecordingRepository : RecordingRepository {
        val inserted = mutableListOf<Recording>()
        val appendedSegments = mutableListOf<TranscriptSegment>()

        override fun observeRecording(id: String) = throw UnsupportedOperationException("not used by StartRecordingUseCase")
        override fun observeRecordings(filter: LibraryFilter) = throw UnsupportedOperationException("not used by StartRecordingUseCase")
        override suspend fun insert(recording: Recording): AppResult<Unit> {
            inserted += recording
            return AppResult.Success(Unit)
        }
        override suspend fun update(recording: Recording): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun updateFilePathAndSize(id: String, filePath: String, sizeBytes: Long): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun appendOrUpdateSegment(recordingId: String, segment: TranscriptSegment): AppResult<Unit> {
            appendedSegments += segment
            return AppResult.Success(Unit)
        }
        override suspend fun replaceSegments(recordingId: String, segments: List<TranscriptSegment>): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun addBookmark(bookmark: Bookmark): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun removeBookmark(bookmarkId: String): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun setFavorite(id: String, isFavorite: Boolean): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun setTags(id: String, tags: List<String>): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun setEncrypted(id: String, isEncrypted: Boolean): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun moveToTrash(id: String, nowEpochMs: Long): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun restoreFromTrash(id: String): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun deletePermanently(id: String): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun purgeTrashOlderThan(cutoffEpochMs: Long): AppResult<Int> = AppResult.Success(0)
    }

    @Test
    fun `invoke prepares the model, starts the foreground service, then emits the transcribed segment`() = runTest {
        val fakeForegroundServiceController = FakeForegroundServiceController()
        val fakeRecordingRepository = FakeRecordingRepository()

        val useCase = StartRecordingUseCase(
            audioCaptureController = fakeAudioCaptureController,
            speechRecognizerProvider = fakeSpeechRecognizerProvider,
            vadProcessor = fakeVadProcessor,
            recordingRepository = fakeRecordingRepository,
            foregroundServiceController = fakeForegroundServiceController,
            idGenerator = { "recording-1" },
            clock = { 1_000L },
        )

        useCase(RecordingConfig()).test {
            assertEquals(RecordingSessionState.PreparingModel, awaitItem())

            val recordingState = awaitItem()
            assertTrue(recordingState is RecordingSessionState.Recording)
            recordingState as RecordingSessionState.Recording
            assertEquals("recording-1", recordingState.recordingId)
            assertEquals("hello world", recordingState.partialText)
            assertEquals(0.6f, recordingState.audioLevel)

            awaitComplete()
        }

        assertTrue(fakeForegroundServiceController.started)
        assertEquals(1, fakeRecordingRepository.inserted.size)
        assertEquals("recording-1", fakeRecordingRepository.inserted.single().id)
        assertEquals(1, fakeRecordingRepository.appendedSegments.size)
    }
}
