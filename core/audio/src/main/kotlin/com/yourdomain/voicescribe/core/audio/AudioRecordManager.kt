package com.yourdomain.voicescribe.core.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.yourdomain.voicescribe.core.domain.model.AudioQuality
import com.yourdomain.voicescribe.core.domain.port.AudioCaptureController
import com.yourdomain.voicescribe.core.domain.port.PcmChunk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sqrt

/**
 * Wraps [android.media.AudioRecord] to stream raw 16-bit PCM for the live
 * STT pipeline. Uses `VOICE_RECOGNITION` audio source, which most OEMs tune
 * for lower noise-suppression latency than `MIC`.
 *
 * Requires [android.Manifest.permission.RECORD_AUDIO] to already be granted;
 * callers (feature:recording's ViewModel) check that before invoking [start].
 */
class AudioRecordManager(private val context: Context) : AudioCaptureController {

    @Volatile private var paused = false
    @Volatile override var isCapturing: Boolean = false
        private set

    private var audioRecord: AudioRecord? = null

    @SuppressLint("MissingPermission")
    override fun start(quality: AudioQuality): Flow<PcmChunk> = callbackFlow {
        val channelConfig = if (quality.channelCount == 1) {
            AudioFormat.CHANNEL_IN_MONO
        } else {
            AudioFormat.CHANNEL_IN_STEREO
        }

        val minBufferSize = AudioRecord.getMinBufferSize(
            quality.sampleRateHz,
            channelConfig,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        check(minBufferSize > 0) { "AudioRecord.getMinBufferSize returned $minBufferSize for $quality" }

        val bufferSizeBytes = minBufferSize * 2
        val record = AudioRecord(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            quality.sampleRateHz,
            channelConfig,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSizeBytes,
        )

        if (record.state != AudioRecord.STATE_INITIALIZED) {
            record.release()
            close(IllegalStateException("AudioRecord failed to initialize"))
            return@callbackFlow
        }

        audioRecord = record
        record.startRecording()
        isCapturing = true
        paused = false

        val readBuffer = ShortArray(bufferSizeBytes / 2)

        val job = launch(Dispatchers.IO) {
            while (isActive && isCapturing) {
                if (paused) {
                    delay(PAUSED_POLL_INTERVAL_MS)
                    continue
                }
                val samplesRead = record.read(readBuffer, 0, readBuffer.size)
                if (samplesRead > 0) {
                    val samples = readBuffer.copyOf(samplesRead)
                    trySend(
                        PcmChunk(
                            samples = samples,
                            rmsLevel = computeNormalizedRms(samples),
                            timestampMs = System.currentTimeMillis(),
                        ),
                    )
                }
            }
        }

        awaitClose {
            job.cancel()
            isCapturing = false
            runCatching { record.stop() }
            record.release()
            audioRecord = null
        }
    }

    override fun pause() {
        paused = true
    }

    override fun resume() {
        paused = false
    }

    override suspend fun stop() {
        isCapturing = false
    }

    private fun computeNormalizedRms(samples: ShortArray): Float {
        if (samples.isEmpty()) return 0f
        var sumSquares = 0.0
        for (sample in samples) sumSquares += sample.toDouble() * sample.toDouble()
        val rms = sqrt(sumSquares / samples.size)
        return (rms / Short.MAX_VALUE).toFloat().coerceIn(0f, 1f)
    }

    private companion object {
        const val PAUSED_POLL_INTERVAL_MS = 50L
    }
}
