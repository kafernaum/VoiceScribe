package com.yourdomain.voicescribe.core.domain.model

/**
 * The available speech-to-text backends. All three run 100% on-device —
 * see STT_ENGINES.md for the full comparison and fallback rules.
 */
enum class SttEngine {
    /** Gemini Nano via ML Kit GenAI Speech Recognition where available, with
     *  an automatic fallback to [SYSTEM_ON_DEVICE] on unsupported hardware. */
    ML_KIT_GENAI_AUTO,

    /** `SpeechRecognizer.createOnDeviceSpeechRecognizer()` — widest device
     *  compatibility (API 31+), no extra model download required. */
    SYSTEM_ON_DEVICE,

    /** Whisper (tiny/base, INT8-quantized) running locally via LiteRT/ONNX
     *  Runtime — for users who want a custom/offline-bundled model. */
    WHISPER_CPP_LOCAL,
}

enum class RecordingMode {
    /** AudioRecord PCM feeding a live STT pipeline. */
    STREAMING,

    /** MediaRecorder compressed file, transcribed afterwards in one batch. */
    FILE,
}

enum class AudioQuality(val sampleRateHz: Int, val channelCount: Int, val bitrateBps: Int) {
    VOICE_STREAMING(sampleRateHz = 16_000, channelCount = 1, bitrateBps = 0),
    FILE_STANDARD(sampleRateHz = 48_000, channelCount = 2, bitrateBps = 48_000),
    FILE_HIGH(sampleRateHz = 48_000, channelCount = 2, bitrateBps = 96_000),
}
