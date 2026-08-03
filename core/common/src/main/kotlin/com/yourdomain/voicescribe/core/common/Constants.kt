package com.yourdomain.voicescribe.core.common

/** App-wide constants that don't belong to any single module. */
object Constants {
    const val PCM_SAMPLE_RATE_STREAMING_HZ = 16_000
    const val PCM_SAMPLE_RATE_FILE_HZ = 48_000
    const val AUDIO_CHANNELS_STREAMING = 1
    const val AUDIO_CHANNELS_FILE = 2

    /** Bookmark auto-inserted when silence exceeds this duration during recording. */
    const val AUTO_BOOKMARK_SILENCE_THRESHOLD_MS = 2_000L

    /** Throttle for persisting partial transcript segments to Room. */
    const val TRANSCRIPT_PERSIST_THROTTLE_MS = 500L

    /** Default trash retention before permanent deletion. */
    const val DEFAULT_TRASH_RETENTION_DAYS = 30

    const val NOTIFICATION_CHANNEL_ID_RECORDING = "recording_channel"
    const val NOTIFICATION_ID_RECORDING = 1001

    const val EXPORT_MIME_TXT = "text/plain"
    const val EXPORT_MIME_SRT = "application/x-subrip"
    const val EXPORT_MIME_VTT = "text/vtt"
    const val EXPORT_MIME_JSON = "application/json"
    const val EXPORT_MIME_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    const val EXPORT_MIME_PDF = "application/pdf"
    const val EXPORT_MIME_ZIP = "application/zip"
}
