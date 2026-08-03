package com.yourdomain.voicescribe.core.domain.model

/**
 * The aggregate root of the app: one recorded session, its audio file
 * location, and everything derived from it (transcript, bookmarks, tags).
 */
data class Recording(
    val id: String,
    val title: String,
    val filePath: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val createdAtEpochMs: Long,
    val language: String,
    val engine: SttEngine,
    val mode: RecordingMode,
    val isEncrypted: Boolean,
    val isFavorite: Boolean = false,
    val isTrashed: Boolean = false,
    val trashedAtEpochMs: Long? = null,
    val tags: List<String> = emptyList(),
    val bookmarks: List<Bookmark> = emptyList(),
    val transcriptSegments: List<TranscriptSegment> = emptyList(),
    val summary: String? = null,
    val wordCount: Int = 0,
    val averageConfidence: Float? = null,
) {
    val fullTranscript: String
        get() = transcriptSegments.filter { it.isFinal }.joinToString(separator = " ") { it.text }

    companion object {
        fun createNew(config: RecordingConfig, id: String, nowEpochMs: Long): Recording = Recording(
            id = id,
            title = config.suggestedTitle ?: "Recording",
            filePath = "",
            durationMs = 0L,
            sizeBytes = 0L,
            createdAtEpochMs = nowEpochMs,
            language = config.locale,
            engine = config.engine,
            mode = config.mode,
            isEncrypted = config.encryptAtRest,
        )
    }
}
