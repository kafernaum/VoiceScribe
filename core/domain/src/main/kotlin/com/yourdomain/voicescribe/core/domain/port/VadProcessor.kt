package com.yourdomain.voicescribe.core.domain.port

import com.yourdomain.voicescribe.core.domain.port.PcmChunk

/** A contiguous span of speech (as opposed to silence) detected by the VAD. */
data class SpeechSegment(
    val pcm: ShortArray,
    val startMs: Long,
    val endMs: Long,
)

enum class VadEvent { SPEECH_START, SPEECH_CONTINUE, SPEECH_END, SILENCE }

/**
 * Voice Activity Detection port. `core:audio` ships two implementations:
 * a Silero VAD (ONNX, ~1 MB model) for accuracy, and a zero-dependency
 * energy-threshold fallback used when the ONNX model asset is unavailable.
 */
interface VadProcessor {
    fun classify(chunk: PcmChunk): VadEvent
    fun reset()
}
