package com.yourdomain.voicescribe.feature.library.export

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.yourdomain.voicescribe.core.common.extensions.toTimestamp
import com.yourdomain.voicescribe.core.domain.model.Recording
import java.io.OutputStream

/**
 * Paginated PDF report built with the platform's own [android.graphics.pdf.PdfDocument]
 * — no third-party PDF library needed (and no AGPL/commercial licensing
 * question either, unlike e.g. iText).
 */
class PdfExporter {

    fun write(recording: Recording, output: OutputStream) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()

        val titlePaint = Paint().apply { textSize = 18f; isFakeBoldText = true }
        val metaPaint = Paint().apply { textSize = 11f; isFakeBoldText = false }
        val bodyPaint = Paint().apply { textSize = 12f }

        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        var y = MARGIN + 18f

        canvas.drawText(recording.title, MARGIN, y, titlePaint)
        y += LINE_HEIGHT * 1.5f
        canvas.drawText(
            "${recording.language} - ${recording.engine.name} - ${recording.durationMs.toTimestamp(includeMillis = false)}",
            MARGIN,
            y,
            metaPaint,
        )
        y += LINE_HEIGHT * 2

        val maxTextWidth = PAGE_WIDTH - 2 * MARGIN

        recording.transcriptSegments.filter { it.isFinal }.forEach { segment ->
            val line = "[${segment.startMs.toTimestamp(includeMillis = false)}] ${segment.text}"
            wrapText(line, bodyPaint, maxTextWidth).forEach { wrappedLine ->
                if (y > PAGE_HEIGHT - MARGIN) {
                    document.finishPage(page)
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    y = MARGIN + 18f
                }
                canvas.drawText(wrappedLine, MARGIN, y, bodyPaint)
                y += LINE_HEIGHT
            }
        }

        document.finishPage(page)
        document.writeTo(output)
        document.close()
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var current = StringBuilder()
        for (word in words) {
            val candidate = if (current.isEmpty()) word else "$current $word"
            if (paint.measureText(candidate) > maxWidth && current.isNotEmpty()) {
                lines += current.toString()
                current = StringBuilder(word)
            } else {
                current = StringBuilder(candidate)
            }
        }
        if (current.isNotEmpty()) lines += current.toString()
        return lines
    }

    private companion object {
        // A4 at 72dpi
        const val PAGE_WIDTH = 595
        const val PAGE_HEIGHT = 842
        const val MARGIN = 40f
        const val LINE_HEIGHT = 16f
    }
}
