package com.yourdomain.voicescribe.feature.library.export

import android.content.Context
import android.net.Uri
import com.yourdomain.voicescribe.core.common.AppError
import com.yourdomain.voicescribe.core.common.AppResult
import com.yourdomain.voicescribe.core.domain.model.ExportFormat
import com.yourdomain.voicescribe.core.domain.model.Recording
import com.yourdomain.voicescribe.core.domain.port.ExportWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * [ExportWriter] adapter: resolves the caller-supplied `content://` (Storage
 * Access Framework) or `file://` URI, then delegates to the format-specific
 * exporter. This is the concrete implementation behind
 * [com.yourdomain.voicescribe.core.domain.usecase.ExportRecordingUseCase].
 */
class ExportManager(
    private val context: Context,
    private val txtExporter: TxtExporter,
    private val srtVttExporter: SrtVttExporter,
    private val jsonExporter: JsonExporter,
    private val docxExporter: DocxExporter,
    private val pdfExporter: PdfExporter,
    private val zipExporter: ZipExporter,
) : ExportWriter {

    override suspend fun export(recording: Recording, format: ExportFormat, destinationUri: String): AppResult<String> =
        withContext(Dispatchers.IO) {
            try {
                val uri = Uri.parse(destinationUri)
                val outputStream = context.contentResolver.openOutputStream(uri)
                    ?: return@withContext AppResult.Failure(
                        AppError.Unknown(IllegalStateException("Cannot open output stream for $destinationUri")),
                    )
                outputStream.use { output -> writeFormat(recording, format, output) }
                AppResult.Success(destinationUri)
            } catch (t: Throwable) {
                AppResult.Failure(AppError.Unknown(t))
            }
        }

    private fun writeFormat(recording: Recording, format: ExportFormat, output: java.io.OutputStream) {
        when (format) {
            ExportFormat.TXT -> txtExporter.write(recording, output)
            ExportFormat.SRT -> srtVttExporter.writeSrt(recording, output)
            ExportFormat.VTT -> srtVttExporter.writeVtt(recording, output)
            ExportFormat.JSON -> jsonExporter.write(recording, output)
            ExportFormat.DOCX -> docxExporter.write(recording, output)
            ExportFormat.PDF -> pdfExporter.write(recording, output)
            ExportFormat.ZIP_BUNDLE -> zipExporter.write(recording, output)
        }
    }
}
