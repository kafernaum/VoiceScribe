package com.yourdomain.voicescribe.core.domain.port

import com.yourdomain.voicescribe.core.common.AppResult
import com.yourdomain.voicescribe.core.domain.model.ExportFormat
import com.yourdomain.voicescribe.core.domain.model.Recording

/**
 * Port implemented by `feature:library`'s export pipeline. Kept in domain so
 * [com.yourdomain.voicescribe.core.domain.usecase.ExportRecordingUseCase] can
 * depend on the abstraction rather than on Storage Access Framework / POI /
 * PdfDocument specifics.
 */
interface ExportWriter {
    /** @param destinationUri a `content://` (SAF) or `file://` URI the caller already has write access to. */
    suspend fun export(recording: Recording, format: ExportFormat, destinationUri: String): AppResult<String>
}
