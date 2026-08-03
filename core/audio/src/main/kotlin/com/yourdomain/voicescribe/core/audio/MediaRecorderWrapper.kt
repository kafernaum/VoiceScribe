package com.yourdomain.voicescribe.core.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import com.yourdomain.voicescribe.core.domain.model.AudioQuality
import com.yourdomain.voicescribe.core.domain.port.FileRecorderController

/**
 * Wraps [android.media.MediaRecorder] for compressed "file mode" recording.
 * Opus-in-Ogg requires API 31+; devices on API 26-30 fall back to AAC-in-MP4
 * (still small, still Play-Store-friendly, just ~2-3x the file size of Opus
 * at the same perceptual quality).
 */
class MediaRecorderWrapper(private val context: Context) : FileRecorderController {

    private var recorder: MediaRecorder? = null
    private var startedAtMs: Long = 0L
    private var pausedAccumMs: Long = 0L
    private var pauseStartedAtMs: Long? = null

    override suspend fun start(outputFilePath: String, quality: AudioQuality) {
        @Suppress("DEPRECATION")
        val newRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }

        newRecorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                setOutputFormat(MediaRecorder.OutputFormat.OGG)
                setAudioEncoder(MediaRecorder.AudioEncoder.OPUS)
            } else {
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            }
            setAudioSamplingRate(quality.sampleRateHz)
            setAudioChannels(quality.channelCount)
            setAudioEncodingBitRate(quality.bitrateBps)
            setOutputFile(outputFilePath)
            prepare()
            start()
        }

        recorder = newRecorder
        startedAtMs = System.currentTimeMillis()
        pausedAccumMs = 0L
        pauseStartedAtMs = null
    }

    override suspend fun pause() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            recorder?.pause()
            pauseStartedAtMs = System.currentTimeMillis()
        }
    }

    override suspend fun resume() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            recorder?.resume()
            pauseStartedAtMs?.let { pausedAccumMs += System.currentTimeMillis() - it }
            pauseStartedAtMs = null
        }
    }

    override suspend fun stop(): Long {
        val activeRecorder = recorder ?: return 0L
        runCatching { activeRecorder.stop() }
        activeRecorder.release()
        recorder = null
        return (System.currentTimeMillis() - startedAtMs) - pausedAccumMs
    }

    companion object {
        /** Pick the output file extension matching what [start] will actually encode. */
        fun recommendedFileExtension(): String =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) "opus" else "m4a"
    }
}
