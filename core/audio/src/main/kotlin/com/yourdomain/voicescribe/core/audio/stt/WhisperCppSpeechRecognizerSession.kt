package com.yourdomain.voicescribe.core.audio.stt

import android.content.Context
import com.yourdomain.voicescribe.core.common.AppError
import com.yourdomain.voicescribe.core.common.AppResult
import com.yourdomain.voicescribe.core.domain.model.SttEngine
import com.yourdomain.voicescribe.core.domain.model.TranscriptSegment
import com.yourdomain.voicescribe.core.domain.port.SpeechRecognizerSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Integration point for a locally-bundled Whisper (tiny/base, INT8) model
 * via LiteRT (TensorFlow Lite) or ONNX Runtime, for users who want a custom
 * or fully offline-bundled model instead of relying on OS/ML-Kit models.
 *
 * **Not implemented in this scaffold** — bundling a Whisper model means
 * shipping a real multi-hundred-MB binary + a native inference runtime,
 * neither of which can be produced as generated source. To finish this:
 *
 * 1. Convert a Whisper tiny/base checkpoint to INT8 LiteRT (`.tflite`) or
 *    ONNX using the official `whisper.cpp` or `openai-whisper` export tools.
 * 2. Ship the model via Play Asset Delivery (on-demand delivery keeps the
 *    base APK under the 50 MB budget from RELEASE_CHECKLIST.md).
 * 3. Replace [transcribeFile] with real mel-spectrogram extraction +
 *    encoder/decoder inference.
 *
 * Until then, [ensureModelDownloaded] reports the model as unavailable so
 * [com.yourdomain.voicescribe.core.audio.stt.SpeechRecognizerFactory] can
 * surface `AppError.SpeechModelNotDownloaded` to the UI rather than silently
 * doing nothing.
 */
class WhisperCppSpeechRecognizerSession(
    private val context: Context,
    private val locale: String,
) : SpeechRecognizerSession {

    override val engine: SttEngine = SttEngine.WHISPER_CPP_LOCAL

    override fun ensureModelDownloaded(): Flow<Float> = flowOf(0f)

    override fun transcribeStreaming(pcm: Flow<ShortArray>): Flow<TranscriptSegment> =
        throw UnsupportedOperationException(
            "WHISPER_CPP_LOCAL is a batch-only engine in this scaffold; select it for file-mode recordings.",
        )

    override suspend fun transcribeFile(filePath: String): AppResult<List<TranscriptSegment>> =
        AppResult.Failure(AppError.SpeechModelNotDownloaded)

    override fun release() = Unit
}
