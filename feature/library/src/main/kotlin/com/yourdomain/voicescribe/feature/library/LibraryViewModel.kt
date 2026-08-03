package com.yourdomain.voicescribe.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourdomain.voicescribe.core.common.onFailure
import com.yourdomain.voicescribe.core.common.onSuccess
import com.yourdomain.voicescribe.core.domain.model.LibraryFilter
import com.yourdomain.voicescribe.core.domain.usecase.DeleteRecordingUseCase
import com.yourdomain.voicescribe.core.domain.usecase.ExportRecordingUseCase
import com.yourdomain.voicescribe.core.domain.usecase.GetRecordingsUseCase
import com.yourdomain.voicescribe.core.domain.usecase.SearchRecordingsUseCase
import com.yourdomain.voicescribe.core.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModel(
    private val getRecordingsUseCase: GetRecordingsUseCase,
    private val searchRecordingsUseCase: SearchRecordingsUseCase,
    private val deleteRecordingUseCase: DeleteRecordingUseCase,
    private val exportRecordingUseCase: ExportRecordingUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
) : ViewModel() {

    private val filterState = MutableStateFlow(LibraryFilter())
    private val selectedIdsState = MutableStateFlow<Set<String>>(emptySet())
    private val loadingState = MutableStateFlow(true)
    private val errorState = MutableStateFlow<String?>(null)

    private val effectChannel = Channel<LibraryEffect>(Channel.BUFFERED)
    val effects: Flow<LibraryEffect> = effectChannel.receiveAsFlow()

    val uiState: StateFlow<LibraryUiState> = combine(
        filterState.flatMapLatest { filter ->
            if (filter.query.isBlank()) getRecordingsUseCase(filter) else searchRecordingsUseCase(filter.query, filter)
        },
        filterState,
        selectedIdsState,
        errorState,
    ) { recordings, filter, selectedIds, error ->
        LibraryUiState(
            recordings = recordings,
            filter = filter,
            isLoading = false,
            selectedIds = selectedIds,
            errorMessage = error,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState())

    fun onIntent(intent: LibraryIntent) {
        when (intent) {
            is LibraryIntent.Search -> filterState.update { it.copy(query = intent.query) }
            is LibraryIntent.ChangeSortOrder -> filterState.update { it.copy(sortOrder = intent.sortOrder) }
            is LibraryIntent.ToggleFavoritesOnly -> filterState.update { it.copy(favoritesOnly = intent.enabled) }
            is LibraryIntent.ToggleFavorite -> toggleFavorite(intent.recordingId, intent.isFavorite)
            is LibraryIntent.MoveToTrash -> moveToTrash(intent.recordingId)
            is LibraryIntent.Export -> export(intent.recordingId, intent.format, intent.destinationUri)
            is LibraryIntent.ToggleSelection -> toggleSelection(intent.recordingId)
            LibraryIntent.ClearSelection -> selectedIdsState.update { emptySet() }
        }
    }

    private fun toggleFavorite(recordingId: String, isFavorite: Boolean) {
        viewModelScope.launch { toggleFavoriteUseCase(recordingId, isFavorite) }
    }

    private fun moveToTrash(recordingId: String) {
        viewModelScope.launch {
            deleteRecordingUseCase.moveToTrash(recordingId, System.currentTimeMillis())
                .onSuccess { sendEffect(LibraryEffect.ShowMessage("Moved to trash")) }
                .onFailure { error -> sendEffect(LibraryEffect.ShowMessage(error.message ?: "Delete failed")) }
        }
    }

    private fun export(recordingId: String, format: com.yourdomain.voicescribe.core.domain.model.ExportFormat, destinationUri: String) {
        viewModelScope.launch {
            exportRecordingUseCase(recordingId, format, destinationUri)
                .onSuccess { path -> sendEffect(LibraryEffect.ShowMessage("Exported to $path")) }
                .onFailure { error -> sendEffect(LibraryEffect.ShowMessage(error.message ?: "Export failed")) }
        }
    }

    private fun toggleSelection(recordingId: String) {
        selectedIdsState.update { current ->
            if (recordingId in current) current - recordingId else current + recordingId
        }
    }

    private fun sendEffect(effect: LibraryEffect) {
        viewModelScope.launch { effectChannel.send(effect) }
    }
}
