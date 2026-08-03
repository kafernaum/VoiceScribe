package com.yourdomain.voicescribe.feature.library.export

import com.yourdomain.voicescribe.core.domain.model.Recording
import java.io.File
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** Bundles the audio file plus every text export format into one .zip. */
class ZipExporter(
    private val txtExporter: TxtExporter,
    private val srtVttExporter: SrtVttExporter,
    private val jsonExporter: JsonExporter,
) {
    fun write(recording: Recording, output: OutputStream) {
        ZipOutputStream(output).use { zip ->
            putText(zip, "transcript.txt", txtExporter.render(recording))
            putText(zip, "transcript.srt", srtVttExporter.renderSrt(recording))
            putText(zip, "transcript.vtt", srtVttExporter.renderVtt(recording))
            putText(zip, "transcript.json", jsonExporter.render(recording))

            val audioFile = File(recording.filePath)
            if (audioFile.exists()) {
                zip.putNextEntry(ZipEntry("audio.${audioFile.extension.ifEmpty { "bin" }}"))
                audioFile.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()
            }
        }
    }

    private fun putText(zip: ZipOutputStream, name: String, content: String) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(content.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }
}
