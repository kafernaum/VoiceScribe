package com.yourdomain.voicescribe.core.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Word-level timings are stored in [wordsEncoded] as a compact delimited
 * string (word, start, end, confidence per token) rather than as JSON, to
 * avoid adding a serialization dependency for what is purely internal
 * storage. See RecordingMapper's encodeWords/decodeWords helpers.
 */
@Entity(
    tableName = "transcript_segments",
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
data class TranscriptSegmentEntity(
    @PrimaryKey val id: String,
    val recordingId: String,
    val startMs: Long,
    val endMs: Long,
    val text: String,
    val confidence: Float?,
    val isFinal: Boolean,
    val wordsEncoded: String,
    val speaker: String?,
)
