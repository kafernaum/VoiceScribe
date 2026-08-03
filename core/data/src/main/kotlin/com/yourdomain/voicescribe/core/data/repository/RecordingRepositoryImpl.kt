package com.yourdomain.voicescribe.core.data.repository

import com.yourdomain.voicescribe.core.common.AppError
import com.yourdomain.voicescribe.core.common.AppResult
import com.yourdomain.voicescribe.core.data.local.db.dao.BookmarkDao
import com.yourdomain.voicescribe.core.data.local.db.dao.RecordingDao
import com.yourdomain.voicescribe.core.data.local.db.dao.TranscriptDao
import com.yourdomain.voicescribe.core.data.local.db.entity.RecordingEntity
import com.yourdomain.voicescribe.core.data.mapper.toDomain
import com.yourdomain.voicescribe.core.data.mapper.toEntity
import com.yourdomain.voicescribe.core.domain.model.Bookmark
import com.yourdomain.voicescribe.core.domain.model.LibraryFilter
import com.yourdomain.voicescribe.core.domain.model.Recording
import com.yourdomain.voicescribe.core.domain.model.SortDirection
import com.yourdomain.voicescribe.core.domain.model.SortField
import com.yourdomain.voicescribe.core.domain.model.TranscriptSegment
import com.yourdomain.voicescribe.core.domain.repository.RecordingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * [RecordingRepository] over Room. List queries ([observeRecordings]) return
 * recordings *without* their bookmarks/transcript segments hydrated — the
 * library screen only needs summary fields, and hydrating every recording's
 * full transcript on every list emission would be an easily-avoided N+1
 * cost. [observeRecording] (single-item, used by the player/detail screens)
 * does the full three-way combine. Filtering beyond full-text search (FTS4)
 * and sorting are applied in-memory for simplicity: correct for a library of
 * up to a few thousand recordings, everything a single user would record.
 */
class RecordingRepositoryImpl(
    private val recordingDao: RecordingDao,
    private val bookmarkDao: BookmarkDao,
    private val transcriptDao: TranscriptDao,
) : RecordingRepository {

    override fun observeRecording(id: String): Flow<Recording?> = combine(
        recordingDao.observeById(id),
        bookmarkDao.observeForRecording(id),
        transcriptDao.observeForRecording(id),
    ) { entity, bookmarks, segments ->
        entity?.toDomain(bookmarks.map { it.toDomain() }, segments.map { it.toDomain() })
    }

    override fun observeRecordings(filter: LibraryFilter): Flow<List<Recording>> {
        val base = if (filter.query.isBlank()) {
            recordingDao.observeAll()
        } else {
            recordingDao.searchFts(toFtsMatchQuery(filter.query))
        }
        return base.map { entities -> applyFilterAndSort(entities, filter).map { it.toDomain() } }
    }

    override suspend fun insert(recording: Recording): AppResult<Unit> = runCatchingResult {
        recordingDao.insert(recording.toEntity())
    }

    override suspend fun update(recording: Recording): AppResult<Unit> = runCatchingResult {
        recordingDao.update(recording.toEntity())
    }

    override suspend fun updateFilePathAndSize(id: String, filePath: String, sizeBytes: Long): AppResult<Unit> =
        runCatchingResult { recordingDao.updateFilePathAndSize(id, filePath, sizeBytes) }

    override suspend fun appendOrUpdateSegment(recordingId: String, segment: TranscriptSegment): AppResult<Unit> =
        runCatchingResult {
            transcriptDao.upsert(segment.toEntity(recordingId))
            refreshSearchableText(recordingId)
        }

    override suspend fun replaceSegments(recordingId: String, segments: List<TranscriptSegment>): AppResult<Unit> =
        runCatchingResult {
            transcriptDao.deleteAllForRecording(recordingId)
            transcriptDao.insertAll(segments.map { it.toEntity(recordingId) })
            refreshSearchableText(recordingId)
        }

    override suspend fun addBookmark(bookmark: Bookmark): AppResult<Unit> =
        runCatchingResult { bookmarkDao.insert(bookmark.toEntity()) }

    override suspend fun removeBookmark(bookmarkId: String): AppResult<Unit> =
        runCatchingResult { bookmarkDao.delete(bookmarkId) }

    override suspend fun setFavorite(id: String, isFavorite: Boolean): AppResult<Unit> =
        runCatchingResult { recordingDao.setFavorite(id, isFavorite) }

    override suspend fun setTags(id: String, tags: List<String>): AppResult<Unit> =
        runCatchingResult { recordingDao.setTags(id, tags.joinToString(",")) }

    override suspend fun setEncrypted(id: String, isEncrypted: Boolean): AppResult<Unit> =
        runCatchingResult { recordingDao.setEncrypted(id, isEncrypted) }

    override suspend fun moveToTrash(id: String, nowEpochMs: Long): AppResult<Unit> =
        runCatchingResult { recordingDao.moveToTrash(id, nowEpochMs) }

    override suspend fun restoreFromTrash(id: String): AppResult<Unit> =
        runCatchingResult { recordingDao.restoreFromTrash(id) }

    override suspend fun deletePermanently(id: String): AppResult<Unit> =
        runCatchingResult { recordingDao.deletePermanently(id) }

    override suspend fun purgeTrashOlderThan(cutoffEpochMs: Long): AppResult<Int> =
        try {
            AppResult.Success(recordingDao.purgeTrashOlderThan(cutoffEpochMs))
        } catch (t: Throwable) {
            AppResult.Failure(AppError.Unknown(t))
        }

    private suspend fun refreshSearchableText(recordingId: String) {
        val segments = transcriptDao.observeForRecording(recordingId).first()
        val fullText = segments.filter { it.isFinal }.joinToString(separator = " ") { it.text }
        recordingDao.updateSearchableText(recordingId, fullText)
    }

    private fun applyFilterAndSort(entities: List<RecordingEntity>, filter: LibraryFilter): List<RecordingEntity> {
        var result = entities.asSequence()
        if (!filter.includeTrashed) result = result.filterNot { it.isTrashed }
        if (filter.favoritesOnly) result = result.filter { it.isFavorite }
        if (filter.encryptedOnly) result = result.filter { it.isEncrypted }
        if (filter.languages.isNotEmpty()) result = result.filter { it.language in filter.languages }
        if (filter.engines.isNotEmpty()) result = result.filter { entity -> filter.engines.any { it.name == entity.engine } }
        filter.dateRangeEpochMs?.let { range -> result = result.filter { it.createdAtEpochMs in range } }

        val comparator = when (filter.sortOrder.field) {
            SortField.DATE -> compareBy<RecordingEntity> { it.createdAtEpochMs }
            SortField.DURATION -> compareBy { it.durationMs }
            SortField.TITLE -> compareBy { it.title.lowercase() }
            SortField.SIZE -> compareBy { it.sizeBytes }
        }
        val ordered = if (filter.sortOrder.direction == SortDirection.DESCENDING) comparator.reversed() else comparator
        return result.sortedWith(ordered).toList()
    }

    private fun toFtsMatchQuery(query: String): String =
        query.trim().split(Regex("\\s+")).joinToString(" ") { token -> "$token*" }

    private inline fun runCatchingResult(block: () -> Unit): AppResult<Unit> = try {
        block()
        AppResult.Success(Unit)
    } catch (t: Throwable) {
        AppResult.Failure(AppError.Unknown(t))
    }
}
