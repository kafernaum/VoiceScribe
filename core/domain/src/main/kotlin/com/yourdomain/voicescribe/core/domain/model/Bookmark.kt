package com.yourdomain.voicescribe.core.domain.model

data class Bookmark(
    val id: String,
    val recordingId: String,
    val positionMs: Long,
    val label: String? = null,
    val isAutoDetected: Boolean = false,
)
