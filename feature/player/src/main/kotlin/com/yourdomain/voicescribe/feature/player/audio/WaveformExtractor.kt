package com.yourdomain.voicescribe.feature.player.audio

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.max

/**
 * Decodes a compressed audio file (anything `MediaExtractor`/`MediaCodec`
 * supports — Opus, AAC, etc.) into a small array of per-bucket amplitude
 * peaks for [com.yourdomain.voicescribe.feature.player.ui.WaveformView],
 * without holding the full decoded PCM in memory. Uses the synchronous
 * `MediaCodec` API; always call from a background dispatcher (see
 * `PlayerViewModel.load`, which runs this on `Dispatchers.IO`).
 */
object WaveformExtractor {

    fun extractPeaks(filePath: String, bucketCount: Int = 200): FloatArray {
        val extractor = MediaExtractor()
        return try {
            extractor.setDataSource(filePath)
            val trackIndex = selectAudioTrack(extractor) ?: return FloatArray(bucketCount)
            extractor.selectTrack(trackIndex)
            decode(extractor, extractor.getTrackFormat(trackIndex), bucketCount)
        } catch (_: Exception) {
            FloatArray(bucketCount)
        } finally {
            extractor.release()
        }
    }

    private fun selectAudioTrack(extractor: MediaExtractor): Int? =
        (0 until extractor.trackCount).firstOrNull { index ->
            extractor.getTrackFormat(index).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
        }

    private fun decode(extractor: MediaExtractor, format: MediaFormat, bucketCount: Int): FloatArray {
        val mime = format.getString(MediaFormat.KEY_MIME) ?: return FloatArray(bucketCount)
        val durationUs = if (format.containsKey(MediaFormat.KEY_DURATION)) format.getLong(MediaFormat.KEY_DURATION) else 1L

        val codec = MediaCodec.createDecoderByType(mime)
        codec.configure(format, null, null, 0)
        codec.start()

        val bucketPeaks = FloatArray(bucketCount)
        val bufferInfo = MediaCodec.BufferInfo()
        var sawInputEos = false
        var sawOutputEos = false

        try {
            while (!sawOutputEos) {
                if (!sawInputEos) {
                    val inputIndex = codec.dequeueInputBuffer(TIMEOUT_US)
                    if (inputIndex >= 0) {
                        val inputBuffer = codec.getInputBuffer(inputIndex)
                        val sampleSize = inputBuffer?.let { extractor.readSampleData(it, 0) } ?: -1
                        if (sampleSize < 0) {
                            codec.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            sawInputEos = true
                        } else {
                            codec.queueInputBuffer(inputIndex, 0, sampleSize, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }

                val outputIndex = codec.dequeueOutputBuffer(bufferInfo, TIMEOUT_US)
                if (outputIndex >= 0) {
                    val outputBuffer = codec.getOutputBuffer(outputIndex)
                    if (outputBuffer != null && bufferInfo.size > 0) {
                        val bucketIndex = if (durationUs > 0) {
                            ((bufferInfo.presentationTimeUs.toDouble() / durationUs) * bucketCount)
                                .toInt()
                                .coerceIn(0, bucketCount - 1)
                        } else {
                            0
                        }
                        val peak = peakOf(outputBuffer)
                        bucketPeaks[bucketIndex] = max(bucketPeaks[bucketIndex], peak)
                    }
                    codec.releaseOutputBuffer(outputIndex, false)
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) sawOutputEos = true
                }
            }
        } finally {
            codec.stop()
            codec.release()
        }

        return bucketPeaks
    }

    private fun peakOf(buffer: java.nio.ByteBuffer): Float {
        val shortBuffer = buffer.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
        var peak = 0f
        var i = 0
        while (i < shortBuffer.remaining()) {
            peak = max(peak, abs(shortBuffer.get(i).toFloat()) / Short.MAX_VALUE)
            i += SAMPLE_STRIDE
        }
        return peak
    }

    private const val TIMEOUT_US = 10_000L
    private const val SAMPLE_STRIDE = 8 // a peak-per-bucket waveform doesn't need every sample
}
