package com.yourdomain.voicescribe.core.audio.stt

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.yourdomain.voicescribe.core.common.AppError
import com.yourdomain.voicescribe.core.common.AppResult
import com.yourdomain.voicescribe.core.domain.model.SttEngine
import com.yourdomain.voicescribe.core.domain.model.TranscriptSegment
import com.yourdomain.voicescribe.core.domain.port.SpeechRecognizerSession
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf

/**
 * Wraps `android.speech.SpeechRecognizer`. On API 31+ this requests
 * `createOnDeviceSpeechRecognizer`, which Android guarantees runs fully
 * on-device; below API 31 it falls back to `createSpeechRecognizer` with
 * `EXTRA_PREFER_OFFLINE`, which is best-effort (not guaranteed offline on
 * every OEM build) — see STT_ENGINES.md for the full compatibility matrix.
 *
 * `android.speech.SpeechRecognizer` owns the microphone itself; it does not
 * accept externally-captured PCM. When this engine is active,
 * `RecordingViewModel` does not route audio through [com.yourdomain.voicescribe.core.audio.AudioRecordManager]
 * for transcription (it may still run AudioRecord in parallel purely for the
 * waveform/VU meter) — see feature:recording's RecordingViewModel.
 */
class SystemOnDeviceSpeechRecognizerSession(
    private val context: Context,
    private val locale: String,
) : SpeechRecognizerSession {

    override val engine: SttEngine = SttEngine.SYSTEM_ON_DEVICE

    override fun ensureModelDownloaded(): Flow<Float> = flowOf(1f)

    override fun transcribeStreaming(pcm: Flow<ShortArray>): Flow<TranscriptSegment> = callbackFlow {
        val recognizer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        } else {
            SpeechRecognizer.createSpeechRecognizer(context)
        }

        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        }

        var segmentIndex = 0

        fun emitSegment(bundle: Bundle, isFinal: Boolean) {
            val text = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull() ?: return
            val confidence = bundle.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)?.firstOrNull()
            trySend(
                TranscriptSegment(
                    id = "segment-$segmentIndex",
                    startMs = 0L,
                    endMs = 0L,
                    text = text,
                    confidence = confidence,
                    isFinal = isFinal,
                ),
            )
            if (isFinal) segmentIndex++
        }

        recognizer.setRecognitionListener(
            object : RecognitionListener {
                override fun onResults(results: Bundle) = emitSegment(results, isFinal = true)
                override fun onPartialResults(partialResults: Bundle) = emitSegment(partialResults, isFinal = false)
                override fun onError(error: Int) {
                    close(SpeechRecognitionException(error))
                }
                override fun onEndOfSpeech() {
                    // Restart listening so the session behaves like continuous
                    // dictation rather than stopping after one utterance.
                    runCatching { recognizer.startListening(recognizerIntent) }
                }
                override fun onReadyForSpeech(params: Bundle?) = Unit
                override fun onBeginningOfSpeech() = Unit
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            },
        )

        recognizer.startListening(recognizerIntent)

        awaitClose { recognizer.destroy() }
    }

    override suspend fun transcribeFile(filePath: String): AppResult<List<TranscriptSegment>> =
        AppResult.Failure(
            AppError.Unknown(
                UnsupportedOperationException(
                    "android.speech.SpeechRecognizer cannot transcribe a file; use ML_KIT_GENAI_AUTO or WHISPER_CPP_LOCAL for batch/file mode.",
                ),
            ),
        )

    override fun release() = Unit

    private class SpeechRecognitionException(errorCode: Int) : Exception("SpeechRecognizer error code $errorCode")
}
