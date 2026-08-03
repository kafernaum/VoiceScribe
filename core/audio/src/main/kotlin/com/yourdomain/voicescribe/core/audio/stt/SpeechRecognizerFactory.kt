package com.yourdomain.voicescribe.core.audio.stt

import android.content.Context
import com.yourdomain.voicescribe.core.domain.model.SttEngine
import com.yourdomain.voicescribe.core.domain.port.SpeechRecognizerProvider
import com.yourdomain.voicescribe.core.domain.port.SpeechRecognizerSession

/** Picks the concrete [SpeechRecognizerSession] for a requested [SttEngine]. */
class SpeechRecognizerFactory(private val context: Context) : SpeechRecognizerProvider {
    override fun getSession(locale: String, engine: SttEngine): SpeechRecognizerSession = when (engine) {
        SttEngine.ML_KIT_GENAI_AUTO -> MlKitGenaiSpeechRecognizerSession(context, locale)
        SttEngine.SYSTEM_ON_DEVICE -> SystemOnDeviceSpeechRecognizerSession(context, locale)
        SttEngine.WHISPER_CPP_LOCAL -> WhisperCppSpeechRecognizerSession(context, locale)
    }
}
