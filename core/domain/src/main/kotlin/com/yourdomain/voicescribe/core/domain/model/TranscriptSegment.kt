package com.yourdomain.voicescribe.core.domain.model

/** A single word (or sub-word token) with device-reported timing, when available. */
data class WordTiming(
    val text: String,
    val startMs: Long,
    val endMs: Long,
    val confidence: Float?,
)

/**
 * One utterance/segment of a transcript. Streaming STT emits many of these
 * per recording, first as [isFinal] = false ("partial") and then finalized.
 */
data class TranscriptSegment(
    val id: String,
    val startMs: Long,
    val endMs: Long,
    val text: String,
    val confidence: Float?,
    val isFinal: Boolean,
    val words: List<WordTiming> = emptyList(),
    val speaker: String? = null,
)
