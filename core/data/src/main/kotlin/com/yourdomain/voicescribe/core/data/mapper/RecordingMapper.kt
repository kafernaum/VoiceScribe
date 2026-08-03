package com.yourdomain.voicescribe.core.data.mapper

import com.yourdomain.voicescribe.core.data.local.db.entity.BookmarkEntity
import com.yourdomain.voicescribe.core.data.local.db.entity.RecordingEntity
import com.yourdomain.voicescribe.core.data.local.db.entity.TranscriptSegmentEntity
import com.yourdomain.voicescribe.core.domain.model.Bookmark
import com.yourdomain.voicescribe.core.domain.model.Recording
import com.yourdomain.voicescribe.core.domain.model.RecordingMode
import com.yourdomain.voicescribe.core.domain.model.SttEngine
import com.yourdomain.voicescribe.core.domain.model.TranscriptSegment
import com.yourdomain.voicescribe.core.domain.model.WordTiming

// Delimiters built from non-printable ASCII codes (31/30/29 = unit/field/record
// separator) rather than typed as literals, so they can never collide with a
// user-entered tag or transcript word. Internal storage format only, never
// serialized outside this module - so plain delimited text is fine here
// instead of pulling in a JSON dependency.
private val TAG_DELIMITER: String = 31.toChar().toString()
private val WORD_FIELD_DELIMITER: String = 30.toChar().toString()
private val WORD_RECORD_DELIMITER: String = 29.toChar().toString()

fun List<String>.toTagsCsv(): String = joinToString(TAG_DELIMITER)
fun String.fromTagsCsv(): List<String> = if (isEmpty()) emptyList() else split(TAG_DELIMITER)

fun List<WordTiming>.encodeWords(): String = joinToString(WORD_RECORD_DELIMITER) { word ->
    listOf(word.text, word.startMs, word.endMs, word.confidence ?: -1f).joinToString(WORD_FIELD_DELIMITER)
}

fun String.decodeWords(): List<WordTiming> {
    if (isEmpty()) return emptyList()
    return split(WORD_RECORD_DELIMITER).mapNotNull { record ->
        val parts = record.split(WORD_FIELD_DELIMITER)
        if (parts.size != 4) return@mapNotNull null
        val confidence = parts[3].toFloatOrNull()?.takeIf { it >= 0f }
        WordTiming(text = parts[0], startMs = parts[1].toLong(), endMs = parts[2].toLong(), confidence = confidence)
    }
}

fun RecordingEntity.toDomain(
    bookmarks: List<Bookmark> = emptyList(),
    segments: List<TranscriptSegment> = emptyList(),
): Recording = Recording(
    id = id,
    title = title,
    filePath = filePath,
    durationMs = durationMs,
    sizeBytes = sizeBytes,
    createdAtEpochMs = createdAtEpochMs,
    language = language,
    engine = runCatching { SttEngine.valueOf(engine) }.getOrDefault(SttEngine.ML_KIT_GENAI_AUTO),
    mode = runCatching { RecordingMode.valueOf(mode) }.getOrDefault(RecordingMode.STREAMING),
    isEncrypted = isEncrypted,
    isFavorite = isFavorite,
    isTrashed = isTrashed,
    trashedAtEpochMs = trashedAtEpochMs,
    tags = tagsCsv.fromTagsCsv(),
    bookmarks = bookmarks,
    transcriptSegments = segments,
    summary = summary,
    wordCount = wordCount,
    averageConfidence = averageConfidence,
)

fun Recording.toEntity(): RecordingEntity = RecordingEntity(
    id = id,
    title = title,
    filePath = filePath,
    durationMs = durationMs,
    sizeBytes = sizeBytes,
    createdAtEpochMs = createdAtEpochMs,
    language = language,
    engine = engine.name,
    mode = mode.name,
    isEncrypted = isEncrypted,
    isFavorite = isFavorite,
    isTrashed = isTrashed,
    trashedAtEpochMs = trashedAtEpochMs,
    tagsCsv = tags.toTagsCsv(),
    summary = summary,
    wordCount = wordCount,
    averageConfidence = averageConfidence,
    searchableText = fullTranscript,
)

fun BookmarkEntity.toDomain(): Bookmark = Bookmark(
    id = id,
    recordingId = recordingId,
    positionMs = positionMs,
    label = label,
    isAutoDetected = isAutoDetected,
)

fun Bookmark.toEntity(): BookmarkEntity = BookmarkEntity(
    id = id,
    recordingId = recordingId,
    positionMs = positionMs,
    label = label,
    isAutoDetected = isAutoDetected,
)

fun TranscriptSegmentEntity.toDomain(): TranscriptSegment = TranscriptSegment(
    id = id,
    startMs = startMs,
    endMs = endMs,
    text = text,
    confidence = confidence,
    isFinal = isFinal,
    words = wordsEncoded.decodeWords(),
    speaker = speaker,
)

fun TranscriptSegment.toEntity(recordingId: String): TranscriptSegmentEntity = TranscriptSegmentEntity(
    id = id,
    recordingId = recordingId,
    startMs = startMs,
    endMs = endMs,
    text = text,
    confidence = confidence,
    isFinal = isFinal,
    wordsEncoded = words.encodeWords(),
    speaker = speaker,
)
