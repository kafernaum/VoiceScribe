package com.yourdomain.voicescribe.core.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yourdomain.voicescribe.core.data.local.db.entity.RecordingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordingDao {

    @Query("SELECT * FROM recordings ORDER BY createdAtEpochMs DESC")
    fun observeAll(): Flow<List<RecordingEntity>>

    @Query("SELECT * FROM recordings WHERE id = :id")
    fun observeById(id: String): Flow<RecordingEntity?>

    @Query(
        """
        SELECT recordings.* FROM recordings
        JOIN recordings_fts ON recordings.rowid = recordings_fts.rowid
        WHERE recordings_fts MATCH :ftsQuery
        """,
    )
    fun searchFts(ftsQuery: String): Flow<List<RecordingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RecordingEntity)

    @Update
    suspend fun update(entity: RecordingEntity)

    @Query("UPDATE recordings SET filePath = :filePath, sizeBytes = :sizeBytes WHERE id = :id")
    suspend fun updateFilePathAndSize(id: String, filePath: String, sizeBytes: Long)

    @Query("UPDATE recordings SET searchableText = :searchableText WHERE id = :id")
    suspend fun updateSearchableText(id: String, searchableText: String)

    @Query("UPDATE recordings SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE recordings SET tagsCsv = :tagsCsv WHERE id = :id")
    suspend fun setTags(id: String, tagsCsv: String)

    @Query("UPDATE recordings SET isEncrypted = :isEncrypted WHERE id = :id")
    suspend fun setEncrypted(id: String, isEncrypted: Boolean)

    @Query("UPDATE recordings SET isTrashed = 1, trashedAtEpochMs = :nowEpochMs WHERE id = :id")
    suspend fun moveToTrash(id: String, nowEpochMs: Long)

    @Query("UPDATE recordings SET isTrashed = 0, trashedAtEpochMs = NULL WHERE id = :id")
    suspend fun restoreFromTrash(id: String)

    @Query("DELETE FROM recordings WHERE id = :id")
    suspend fun deletePermanently(id: String)

    @Query("DELETE FROM recordings WHERE isTrashed = 1 AND trashedAtEpochMs < :cutoffEpochMs")
    suspend fun purgeTrashOlderThan(cutoffEpochMs: Long): Int
}
