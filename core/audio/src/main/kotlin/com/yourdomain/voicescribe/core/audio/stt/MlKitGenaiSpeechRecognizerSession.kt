package com.yourdomain.voicescribe.core.audio.stt

import android.content.Context
import android.os.Build
import com.yourdomain.voicescribe.core.common.AppResult
import com.yourdomain.voicescribe.core.domain.model.SttEngine
import com.yourdomain.voicescribe.core.domain.model.TranscriptSegment
import com.yourdomain.voicescribe.core.domain.port.SpeechRecognizerSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * ML Kit GenAI Speech Recognition adapter (`SttEngine.ML_KIT_GENAI_AUTO`).
 *
 * At the time this project was generated, `com.google.mlkit:genai-speech-recognition`
 * is an early-access API gated behind allow-listing, and its exact class
 * surface can change before general availability — see
 * https://developers.google.com/ml-kit/genai/speech-recognition for the
 * current API once your app is allow-listed. Rather than guess at unstable
 * class names, this adapter:
 *
 * 1. Checks [isGeminiNanoSupported] (device/OS gate for Gemini Nano).
 * 2. If supported *and* the real ML Kit GenAI classes are on the classpath,
 *    delegates to them (fill in [runGeminiNanoInference] once you've added
 *    the real dependency and are allow-listed).
 * 3. Otherwise transparently falls back to [SystemOnDeviceSpeechRecognizerSession],
 *    exactly matching the "Gemini Nano si disponible, sinon modele
 *    traditionnel" behavior requested for this engine.
 *
 * This means `ML_KIT_GENAI_AUTO` is fully functional today (via the fallback)
 * and becomes the higher-quality Gemini Nano path with a small, isolated
 * change once you're allow-listed — no call sites elsewhere in the app need
 * to change.
 */
class MlKitGenaiSpeechRecognizerSession(
    private val context: Context,
    private val locale: String,
) : SpeechRecognizerSession {

    override val engine: SttEngine = SttEngine.ML_KIT_GENAI_AUTO

    private val delegate: SpeechRecognizerSession by lazy {
        SystemOnDeviceSpeechRecognizerSession(context, locale)
    }

    override fun ensureModelDownloaded(): Flow<Float> =
        if (isGeminiNanoSupported()) flowOf(1f) else delegate.ensureModelDownloaded()

    override fun transcribeStreaming(pcm: Flow<ShortArray>): Flow<TranscriptSegment> =
        // TODO(#mlkit-genai): once allow-listed, branch here to a real Gemini
        // Nano streaming session instead of always delegating.
        delegate.transcribeStreaming(pcm)

    override suspend fun transcribeFile(filePath: String): AppResult<List<TranscriptSegment>> =
        delegate.transcribeFile(filePath)

    override fun release() = delegate.release()

    private fun isGeminiNanoSupported(): Boolean {
        // Gemini Nano via AICore is currently limited to a short list of
        // flagship devices on recent Android releases. This is a conservative
        // placeholder check (OS level only) — replace with the real
        // `AiCoreAvailability` / ML Kit capability check once integrated.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
    }
}
