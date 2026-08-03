package com.yourdomain.voicescribe.feature.library.export

import com.yourdomain.voicescribe.core.common.extensions.toTimestamp
import com.yourdomain.voicescribe.core.domain.model.Recording
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Writes a minimal, valid OOXML `.docx` by hand (a docx is just a zip of a
 * few XML parts). Deliberately avoids Apache POI: POI's `.docx` support
 * pulls in `java.awt`-dependent classes that are unavailable on Android,
 * which makes it unreliable in real apps — see
 * docs/adrs/0006-hand-rolled-docx-not-poi.md.
 */
class DocxExporter {

    fun write(recording: Recording, output: OutputStream) {
        ZipOutputStream(output).use { zip ->
            writeEntry(zip, "[Content_Types].xml", contentTypesXml())
            writeEntry(zip, "_rels/.rels", relsXml())
            writeEntry(zip, "word/document.xml", documentXml(recording))
        }
    }

    private fun writeEntry(zip: ZipOutputStream, name: String, content: String) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(content.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    private fun contentTypesXml(): String = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
          <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
          <Default Extension="xml" ContentType="application/xml"/>
          <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
        </Types>
    """.trimIndent()

    private fun relsXml(): String = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
          <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
        </Relationships>
    """.trimIndent()

    private fun documentXml(recording: Recording): String {
        val titleParagraph = paragraph(recording.title, bold = true, sizeHalfPoints = 32)
        val metaParagraph = paragraph(
            "${recording.language} - ${recording.engine.name} - ${recording.durationMs.toTimestamp(includeMillis = false)}",
            italic = true,
        )
        val bodyParagraphs = recording.transcriptSegments.filter { it.isFinal }.joinToString(separator = "") { segment ->
            val speakerPrefix = segment.speaker?.let { "[$it] " } ?: ""
            val timePrefix = "[${segment.startMs.toTimestamp(includeMillis = false)}] "
            paragraph(timePrefix + speakerPrefix + segment.text)
        }
        return """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
              <w:body>
                $titleParagraph
                $metaParagraph
                $bodyParagraphs
              </w:body>
            </w:document>
        """.trimIndent()
    }

    private fun paragraph(text: String, bold: Boolean = false, italic: Boolean = false, sizeHalfPoints: Int? = null): String {
        val runProperties = buildString {
            if (bold) append("<w:b/>")
            if (italic) append("<w:i/>")
            sizeHalfPoints?.let { append("<w:sz w:val=\"$it\"/>") }
        }
        val runPropertiesTag = if (runProperties.isNotEmpty()) "<w:rPr>$runProperties</w:rPr>" else ""
        return "<w:p><w:r>$runPropertiesTag<w:t xml:space=\"preserve\">${escapeXml(text)}</w:t></w:r></w:p>"
    }

    private fun escapeXml(value: String): String = value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
}
