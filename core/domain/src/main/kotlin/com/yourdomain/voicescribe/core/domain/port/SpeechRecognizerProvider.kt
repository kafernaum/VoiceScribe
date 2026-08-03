package com.yourdomain.voicescribe.core.domain.port

import com.yourdomain.voicescribe.core.common.AppResult
import com.yourdomain.voicescribe.core.domain.model.SttEngine
import com.yourdomain.voicescribe.core.domain.model.TranscriptSegment
import kotlinx.coroutines.flow.Flow

/** A ready-to-use recognizer session bound to one locale + engine. */
interface SpeechRecognizerSession {
    val engine: SttEngine

    /** Ensures the on-device model is present, emitting download progress in [0f, 1f]. */
    fun ensureModelDownloaded(): Flow<Float>

    /** Streams PCM in, transcript segments out (partial then final). */
    fun transcribeStreaming(pcm: Flow<ShortArray>): Flow<TranscriptSegment>

    /** Batch/offline transcription of an already-recorded file. */
    suspend fun transcribeFile(filePath: String): AppResult<List<TranscriptSegment>>

    fun release()
}

/**
 * Factory port that picks (or is told to pick) a concrete STT backend.
 * `core:audio`'s `SpeechRecognizerFactory` implements this by trying
 * ML Kit GenAI first, then falling back per STT_ENGINES.md.
 */
interface SpeechRecognizerProvider {
    fun getSession(locale: String, engine: SttEngine): SpeechRecognizerSession
}
