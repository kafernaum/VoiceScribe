package com.yourdomain.voicescribe.core.domain.model

/** Everything needed to start a new recording session. */
data class RecordingConfig(
    val locale: String = "en-US",
    val engine: SttEngine = SttEngine.ML_KIT_GENAI_AUTO,
    val mode: RecordingMode = RecordingMode.STREAMING,
    val quality: AudioQuality = AudioQuality.VOICE_STREAMING,
    val vadEnabled: Boolean = true,
    val encryptAtRest: Boolean = true,
    val incognito: Boolean = false,
    val suggestedTitle: String? = null,
)
