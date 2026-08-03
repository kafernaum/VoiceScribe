package com.yourdomain.voicescribe.core.domain.usecase

import com.yourdomain.voicescribe.core.domain.model.LibraryFilter
import com.yourdomain.voicescribe.core.domain.model.Recording
import com.yourdomain.voicescribe.core.domain.repository.RecordingRepository
import kotlinx.coroutines.flow.Flow

class GetRecordingsUseCase(private val recordingRepository: RecordingRepository) {
    operator fun invoke(filter: LibraryFilter = LibraryFilter()): Flow<List<Recording>> =
        recordingRepository.observeRecordings(filter)
}
