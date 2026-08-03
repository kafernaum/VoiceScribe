package com.yourdomain.voicescribe.core.data.local.db.entity

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

/**
 * Room representation of the Recording domain model. tagsCsv and
 * searchableText are deliberately denormalized: the former avoids a join
 * table for what is, in practice, a handful of short tags per recording;
 * the latter mirrors the concatenated final transcript so full text search
 * does not need to join across transcript_segments (see RecordingFtsEntity
 * and docs/adrs/0004-fts4-instead-of-fts5.md).
 */
@Entity(tableName = "recordings")
data class RecordingEntity(
    @PrimaryKey val id: String,
    val title: String,
    val filePath: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val createdAtEpochMs: Long,
    val language: String,
    val engine: String,
    val mode: String,
    val isEncrypted: Boolean,
    val isFavorite: Boolean,
    val isTrashed: Boolean,
    val trashedAtEpochMs: Long?,
    val tagsCsv: String,
    val summary: String?,
    val wordCount: Int,
    val averageConfidence: Float?,
    val searchableText: String,
)

/**
 * External-content FTS4 virtual table kept in sync with RecordingEntity by
 * Room-generated triggers. Column names must match a subset of the content
 * entity's columns exactly.
 */
@Fts4(contentEntity = RecordingEntity::class)
@Entity(tableName = "recordings_fts")
data class RecordingFtsEntity(
    val title: String,
    val searchableText: String,
    val tagsCsv: String,
)
