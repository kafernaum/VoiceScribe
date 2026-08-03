package com.yourdomain.voicescribe.core.domain.repository

import com.yourdomain.voicescribe.core.common.AppResult
import com.yourdomain.voicescribe.core.domain.model.Bookmark
import com.yourdomain.voicescribe.core.domain.model.LibraryFilter
import com.yourdomain.voicescribe.core.domain.model.Recording
import com.yourdomain.voicescribe.core.domain.model.TranscriptSegment
import kotlinx.coroutines.flow.Flow

/**
 * Repository port for recordings + their transcripts/bookmarks. Implemented
 * in `core:data` on top of Room; the domain layer never sees SQL or entities.
 */
interface RecordingRepository {
    fun observeRecording(id: String): Flow<Recording?>
    fun observeRecordings(filter: LibraryFilter): Flow<List<Recording>>

    suspend fun insert(recording: Recording): AppResult<Unit>
    suspend fun update(recording: Recording): AppResult<Unit>
    suspend fun updateFilePathAndSize(id: String, filePath: String, sizeBytes: Long): AppResult<Unit>

    suspend fun appendOrUpdateSegment(recordingId: String, segment: TranscriptSegment): AppResult<Unit>
    suspend fun replaceSegments(recordingId: String, segments: List<TranscriptSegment>): AppResult<Unit>

    suspend fun addBookmark(bookmark: Bookmark): AppResult<Unit>
    suspend fun removeBookmark(bookmarkId: String): AppResult<Unit>

    suspend fun setFavorite(id: String, isFavorite: Boolean): AppResult<Unit>
    suspend fun setTags(id: String, tags: List<String>): AppResult<Unit>
    suspend fun setEncrypted(id: String, isEncrypted: Boolean): AppResult<Unit>

    suspend fun moveToTrash(id: String, nowEpochMs: Long): AppResult<Unit>
    suspend fun restoreFromTrash(id: String): AppResult<Unit>
    suspend fun deletePermanently(id: String): AppResult<Unit>
    suspend fun purgeTrashOlderThan(cutoffEpochMs: Long): AppResult<Int>
}
