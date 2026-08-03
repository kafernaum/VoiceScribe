package com.yourdomain.voicescribe.core.domain.port

import com.yourdomain.voicescribe.core.domain.model.AudioQuality
import kotlinx.coroutines.flow.Flow

/** One chunk of raw PCM audio plus the running RMS level, for the VU meter/waveform. */
data class PcmChunk(
    val samples: ShortArray,
    val rmsLevel: Float,
    val timestampMs: Long,
)

/**
 * Port over the platform audio capture primitive. The real implementation
 * (`core:audio`'s `AudioRecordManager`) wraps `android.media.AudioRecord`;
 * this interface has no Android import so `StartRecordingUseCase` stays
 * unit-testable with a fake.
 */
interface AudioCaptureController {
    val isCapturing: Boolean

    fun start(quality: AudioQuality): Flow<PcmChunk>
    fun pause()
    fun resume()
    suspend fun stop()
}

/** Port over `android.media.MediaRecorder` for compressed file capture. */
interface FileRecorderController {
    suspend fun start(outputFilePath: String, quality: AudioQuality)
    suspend fun pause()
    suspend fun resume()
    suspend fun stop(): Long // returns final duration in ms
}
