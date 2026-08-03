package com.yourdomain.voicescribe.feature.recording

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourdomain.voicescribe.core.domain.model.RecordingConfig
import com.yourdomain.voicescribe.core.domain.model.RecordingSessionState
import com.yourdomain.voicescribe.core.domain.usecase.AddBookmarkUseCase
import com.yourdomain.voicescribe.core.domain.usecase.PauseResumeRecordingUseCase
import com.yourdomain.voicescribe.core.domain.usecase.StartRecordingUseCase
import com.yourdomain.voicescribe.core.domain.usecase.StopRecordingUseCase
import com.yourdomain.voicescribe.core.common.onFailure
import com.yourdomain.voicescribe.core.common.onSuccess
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVI ViewModel driving the recording screen. All business logic lives in
 * the injected use cases (core:domain); this class only reduces
 * [RecordingSessionState] emissions into [RecordingUiState] and forwards
 * user [RecordingIntent]s.
 */
class RecordingViewModel(
    private val startRecordingUseCase: StartRecordingUseCase,
    private val stopRecordingUseCase: StopRecordingUseCase,
    private val pauseResumeRecordingUseCase: PauseResumeRecordingUseCase,
    private val addBookmarkUseCase: AddBookmarkUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordingUiState())
    val uiState: StateFlow<RecordingUiState> = _uiState.asStateFlow()

    private val effectChannel = Channel<RecordingEffect>(Channel.BUFFERED)
    val effects: Flow<RecordingEffect> = effectChannel.receiveAsFlow()

    private var sessionJob: Job? = null

    fun onIntent(intent: RecordingIntent) {
        when (intent) {
            is RecordingIntent.StartRecording -> startRecording(intent.config)
            RecordingIntent.PauseOrResume -> togglePauseResume()
            RecordingIntent.Stop -> stopRecording()
            is RecordingIntent.AddBookmark -> addBookmark(intent.label)
            RecordingIntent.Close -> sendEffect(RecordingEffect.NavigateToLibrary)
            RecordingIntent.DismissError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun startRecording(config: RecordingConfig) {
        if (sessionJob?.isActive == true) return
        _uiState.update { it.copy(phase = RecordingUiState.Phase.PREPARING, config = config, errorMessage = null) }

        sessionJob = startRecordingUseCase(config)
            .onEach(::reduceSessionState)
            .catch { throwable ->
                _uiState.update {
                    it.copy(phase = RecordingUiState.Phase.ERROR, errorMessage = throwable.message ?: "Unknown error")
                }
            }
            .launchIn(viewModelScope)
    }

    private fun reduceSessionState(sessionState: RecordingSessionState) {
        _uiState.update { current ->
            when (sessionState) {
                RecordingSessionState.Idle -> current.copy(phase = RecordingUiState.Phase.IDLE)
                RecordingSessionState.PreparingModel -> current.copy(phase = RecordingUiState.Phase.PREPARING)
                is RecordingSessionState.Recording -> current.copy(
                    phase = RecordingUiState.Phase.RECORDING,
                    recordingId = sessionState.recordingId,
                    elapsedMs = sessionState.elapsedMs,
                    audioLevel = sessionState.audioLevel,
                    partialText = sessionState.partialText,
                    segments = sessionState.segments,
                    isPaused = sessionState.isPaused,
                )
                is RecordingSessionState.Stopped -> current.copy(phase = RecordingUiState.Phase.STOPPED)
                is RecordingSessionState.Error -> current.copy(
                    phase = RecordingUiState.Phase.ERROR,
                    errorMessage = sessionState.error.message ?: sessionState.error::class.simpleName,
                )
            }
        }
    }

    private fun togglePauseResume() {
        if (pauseResumeRecordingUseCase.isPaused) {
            pauseResumeRecordingUseCase.resume()
        } else {
            pauseResumeRecordingUseCase.pause()
        }
        _uiState.update { it.copy(isPaused = pauseResumeRecordingUseCase.isPaused) }
    }

    private fun stopRecording() {
        val recordingId = _uiState.value.recordingId ?: return
        viewModelScope.launch {
            sessionJob?.cancel()
            stopRecordingUseCase(recordingId)
                .onSuccess { _uiState.update { it.copy(phase = RecordingUiState.Phase.STOPPED) } }
                .onFailure { error ->
                    _uiState.update { it.copy(phase = RecordingUiState.Phase.ERROR, errorMessage = error.message) }
                }
            effectChannel.send(RecordingEffect.NavigateToLibrary)
        }
    }

    private fun addBookmark(label: String?) {
        val recordingId = _uiState.value.recordingId ?: return
        viewModelScope.launch {
            addBookmarkUseCase(recordingId, _uiState.value.elapsedMs, label)
                .onSuccess { sendEffect(RecordingEffect.ShowMessage("Bookmark added")) }
        }
    }

    private fun sendEffect(effect: RecordingEffect) {
        viewModelScope.launch { effectChannel.send(effect) }
    }

    override fun onCleared() {
        sessionJob?.cancel()
        super.onCleared()
    }
}
