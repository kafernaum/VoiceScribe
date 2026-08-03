package com.yourdomain.voicescribe.core.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bookmarks",
    foreignKeys = [
        ForeignKey(
            entity = RecordingEntity::class,
            parentColumns = ["id"],
            childColumns = ["recordingId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("recordingId")],
)
data class BookmarkEntity(
    @PrimaryKey val id: String,
    val recordingId: String,
    val positionMs: Long,
    val label: String?,
    val isAutoDetected: Boolean,
)
