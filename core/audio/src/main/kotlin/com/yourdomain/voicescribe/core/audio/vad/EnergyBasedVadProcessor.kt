package com.yourdomain.voicescribe.core.audio.vad

import com.yourdomain.voicescribe.core.domain.port.PcmChunk
import com.yourdomain.voicescribe.core.domain.port.VadEvent
import com.yourdomain.voicescribe.core.domain.port.VadProcessor

/**
 * Zero-dependency VAD fallback: a normalized-RMS threshold with a hangover
 * window so brief pauses inside a sentence don't get chopped into separate
 * segments. Ships as the default; [SileroVadProcessor] can be swapped in via
 * Settings once its ONNX model asset is bundled.
 */
class EnergyBasedVadProcessor(
    private val energyThreshold: Float = DEFAULT_ENERGY_THRESHOLD,
    private val hangoverFrames: Int = DEFAULT_HANGOVER_FRAMES,
) : VadProcessor {

    private var consecutiveSilentFrames = 0
    private var isSpeaking = false

    override fun classify(chunk: PcmChunk): VadEvent {
        val isLoudEnough = chunk.rmsLevel >= energyThreshold

        if (isLoudEnough) {
            consecutiveSilentFrames = 0
            val event = if (isSpeaking) VadEvent.SPEECH_CONTINUE else VadEvent.SPEECH_START
            isSpeaking = true
            return event
        }

        if (!isSpeaking) return VadEvent.SILENCE

        consecutiveSilentFrames++
        return if (consecutiveSilentFrames >= hangoverFrames) {
            isSpeaking = false
            VadEvent.SPEECH_END
        } else {
            VadEvent.SPEECH_CONTINUE
        }
    }

    override fun reset() {
        consecutiveSilentFrames = 0
        isSpeaking = false
    }

    private companion object {
        const val DEFAULT_ENERGY_THRESHOLD = 0.02f
        const val DEFAULT_HANGOVER_FRAMES = 8
    }
}
