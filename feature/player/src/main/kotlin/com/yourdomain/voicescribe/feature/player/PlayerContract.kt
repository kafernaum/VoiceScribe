package com.yourdomain.voicescribe.feature.player

import com.yourdomain.voicescribe.core.domain.model.Recording

data class PlaybackState(
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val speed: Float = 1.0f,
    val skipSilenceEnabled: Boolean = false,
    val waveformPeaks: FloatArray = FloatArray(0),
) {
    // FloatArray has no structural equals(); provided explicitly so data
    // class-generated equals()/hashCode() don't break Compose state diffing.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlaybackState) return false
        return isPlaying == other.isPlaying &&
            currentPositionMs == other.currentPositionMs &&
            speed == other.speed &&
            skipSilenceEnabled == other.skipSilenceEnabled &&
            waveformPeaks.contentEquals(other.waveformPeaks)
    }

    override fun hashCode(): Int {
        var result = isPlaying.hashCode()
        result = 31 * result + currentPositionMs.hashCode()
        result = 31 * result + speed.hashCode()
        result = 31 * result + skipSilenceEnabled.hashCode()
        result = 31 * result + waveformPeaks.contentHashCode()
        return result
    }
}

data class PlayerUiState(
    val recording: Recording? = null,
    val playback: PlaybackState = PlaybackState(),
)

sealed interface PlayerIntent {
    data class Load(val recordingId: String) : PlayerIntent
    data object PlayPause : PlayerIntent
    data class SeekTo(val positionMs: Long) : PlayerIntent
    data class SetSpeed(val speed: Float) : PlayerIntent
    data class SetSkipSilence(val enabled: Boolean) : PlayerIntent
    data class AddBookmark(val label: String? = null) : PlayerIntent
}

val AVAILABLE_PLAYBACK_SPEEDS = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f, 3.0f)
