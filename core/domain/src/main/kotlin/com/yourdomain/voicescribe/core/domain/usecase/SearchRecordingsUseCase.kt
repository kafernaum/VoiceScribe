package com.yourdomain.voicescribe.core.domain.usecase

import com.yourdomain.voicescribe.core.domain.model.LibraryFilter
import com.yourdomain.voicescribe.core.domain.model.Recording
import com.yourdomain.voicescribe.core.domain.repository.RecordingRepository
import kotlinx.coroutines.flow.Flow

/**
 * Thin, discoverable wrapper around [GetRecordingsUseCase] for full-text
 * search (backed by the `recording_fts` virtual table — see
 * core:data's RecordingDao and docs/adrs/0004-fts4-instead-of-fts5.md).
 */
class SearchRecordingsUseCase(private val recordingRepository: RecordingRepository) {
    operator fun invoke(query: String, baseFilter: LibraryFilter = LibraryFilter()): Flow<List<Recording>> =
        recordingRepository.observeRecordings(baseFilter.copy(query = query))
}
