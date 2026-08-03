package com.yourdomain.voicescribe.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.yourdomain.voicescribe.core.domain.usecase.AddBookmarkUseCase
import com.yourdomain.voicescribe.core.domain.usecase.ObserveRecordingUseCase
import com.yourdomain.voicescribe.feature.player.audio.WaveformExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModel(
    private val observeRecordingUseCase: ObserveRecordingUseCase,
    private val addBookmarkUseCase: AddBookmarkUseCase,
    private val exoPlayer: ExoPlayer,
) : ViewModel() {

    private val recordingIdState = MutableStateFlow<String?>(null)
    private val playbackState = MutableStateFlow(PlaybackState())
    private var positionTickerJob: Job? = null

    val uiState: StateFlow<PlayerUiState> = combine(
        recordingIdState.flatMapLatest { id -> if (id == null) flowOf(null) else observeRecordingUseCase(id) },
        playbackState,
    ) { recording, playback -> PlayerUiState(recording = recording, playback = playback) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlayerUiState())

    fun onIntent(intent: PlayerIntent) {
        when (intent) {
            is PlayerIntent.Load -> load(intent.recordingId)
            PlayerIntent.PlayPause -> playPause()
            is PlayerIntent.SeekTo -> seekTo(intent.positionMs)
            is PlayerIntent.SetSpeed -> setSpeed(intent.speed)
            is PlayerIntent.SetSkipSilence -> setSkipSilence(intent.enabled)
            is PlayerIntent.AddBookmark -> addBookmark(intent.label)
        }
    }

    private fun load(recordingId: String) {
        recordingIdState.value = recordingId
        viewModelScope.launch {
            val recording = observeRecordingUseCase(recordingId).first() ?: return@launch

            exoPlayer.setMediaItem(MediaItem.fromUri(recording.filePath))
            exoPlayer.prepare()

            val peaks = withContext(Dispatchers.IO) { WaveformExtractor.extractPeaks(recording.filePath) }
            playbackState.update { it.copy(waveformPeaks = peaks) }

            startPositionTicker()
        }
    }

    private fun startPositionTicker() {
        positionTickerJob?.cancel()
        positionTickerJob = viewModelScope.launch {
            while (isActive) {
                playbackState.update {
                    it.copy(isPlaying = exoPlayer.isPlaying, currentPositionMs = exoPlayer.currentPosition)
                }
                delay(POSITION_TICK_MS)
            }
        }
    }

    private fun playPause() {
        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
    }

    private fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
        playbackState.update { it.copy(currentPositionMs = positionMs) }
    }

    private fun setSpeed(speed: Float) {
        exoPlayer.setPlaybackSpeed(speed)
        playbackState.update { it.copy(speed = speed) }
    }

    private fun setSkipSilence(enabled: Boolean) {
        exoPlayer.skipSilenceEnabled = enabled
        playbackState.update { it.copy(skipSilenceEnabled = enabled) }
    }

    private fun addBookmark(label: String?) {
        val recordingId = recordingIdState.value ?: return
        viewModelScope.launch {
            addBookmarkUseCase(recordingId, exoPlayer.currentPosition, label)
        }
    }

    override fun onCleared() {
        positionTickerJob?.cancel()
        exoPlayer.release()
        super.onCleared()
    }

    private companion object {
        const val POSITION_TICK_MS = 100L
    }
}
