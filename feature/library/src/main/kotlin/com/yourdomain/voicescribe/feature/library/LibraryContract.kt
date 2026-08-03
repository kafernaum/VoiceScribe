package com.yourdomain.voicescribe.feature.library

import com.yourdomain.voicescribe.core.domain.model.ExportFormat
import com.yourdomain.voicescribe.core.domain.model.LibraryFilter
import com.yourdomain.voicescribe.core.domain.model.Recording
import com.yourdomain.voicescribe.core.domain.model.SortOrder

sealed interface LibraryIntent {
    data class Search(val query: String) : LibraryIntent
    data class ChangeSortOrder(val sortOrder: SortOrder) : LibraryIntent
    data class ToggleFavoritesOnly(val enabled: Boolean) : LibraryIntent
    data class ToggleFavorite(val recordingId: String, val isFavorite: Boolean) : LibraryIntent
    data class MoveToTrash(val recordingId: String) : LibraryIntent
    data class Export(val recordingId: String, val format: ExportFormat, val destinationUri: String) : LibraryIntent
    data class ToggleSelection(val recordingId: String) : LibraryIntent
    data object ClearSelection : LibraryIntent
}

data class LibraryUiState(
    val recordings: List<Recording> = emptyList(),
    val filter: LibraryFilter = LibraryFilter(),
    val isLoading: Boolean = true,
    val selectedIds: Set<String> = emptySet(),
    val lastExportedPath: String? = null,
    val errorMessage: String? = null,
) {
    val isSelectionMode: Boolean get() = selectedIds.isNotEmpty()
}

sealed interface LibraryEffect {
    data class ShowMessage(val message: String) : LibraryEffect
    data class RequestExportDestination(val recordingId: String, val format: ExportFormat) : LibraryEffect
}
