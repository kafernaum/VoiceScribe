package com.yourdomain.voicescribe.core.audio.vad

import android.content.Context
import com.yourdomain.voicescribe.core.domain.port.PcmChunk
import com.yourdomain.voicescribe.core.domain.port.VadEvent
import com.yourdomain.voicescribe.core.domain.port.VadProcessor

/**
 * Integration point for Silero VAD (ONNX Runtime, ~1 MB model) as described
 * in the project brief. **Not wired to a real ONNX session in this scaffold**
 * — the model binary (`silero_vad.onnx`) is not something that can be
 * generated as source code, so it isn't bundled here. To finish this
 * integration:
 *
 * 1. Download `silero_vad.onnx` from https://github.com/snakers4/silero-vad
 *    and place it at `core/audio/src/main/assets/models/silero_vad.onnx`.
 * 2. Uncomment the `onnxruntime-android` dependency in
 *    `core/audio/build.gradle.kts`.
 * 3. Replace the body of [classify] with a real `OrtSession.run(...)` call —
 *    Silero's ONNX graph expects a `[1, N]` float tensor of samples
 *    normalized to [-1, 1] (divide [PcmChunk.samples] by 32768) plus the
 *    sample rate and recurrent state tensors; consult the model version's
 *    documented I/O signature, since it has changed across Silero releases.
 *
 * Until then this class transparently delegates to [fallback], so selecting
 * "Silero VAD" in Settings never breaks recording — it just behaves like the
 * energy-based VAD until the steps above are completed.
 */
class SileroVadProcessor(
    context: Context,
    private val fallback: VadProcessor = EnergyBasedVadProcessor(),
) : VadProcessor {

    private val modelAvailable: Boolean = runCatching {
        context.assets.open(MODEL_ASSET_PATH).use { true }
    }.getOrDefault(false)

    override fun classify(chunk: PcmChunk): VadEvent {
        // TODO(#silero-vad): run real ONNX inference once modelAvailable is true.
        return fallback.classify(chunk)
    }

    override fun reset() = fallback.reset()

    companion object {
        const val MODEL_ASSET_PATH = "models/silero_vad.onnx"
    }
}
