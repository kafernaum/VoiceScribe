package com.yourdomain.voicescribe.feature.library.export

import com.yourdomain.voicescribe.core.common.extensions.toSrtTimestamp
import com.yourdomain.voicescribe.core.common.extensions.toTimestamp
import com.yourdomain.voicescribe.core.domain.model.Recording
import java.io.OutputStream

class SrtVttExporter {

    fun renderSrt(recording: Recording): String = buildString {
        val finalSegments = recording.transcriptSegments.filter { it.isFinal }
        finalSegments.forEachIndexed { index, segment ->
            appendLine(index + 1)
            appendLine("${segment.startMs.toSrtTimestamp()} --> ${segment.endMs.toSrtTimestamp()}")
            appendLine(segment.text)
            appendLine()
        }
    }

    fun renderVtt(recording: Recording): String = buildString {
        appendLine("WEBVTT")
        appendLine()
        recording.transcriptSegments.filter { it.isFinal }.forEach { segment ->
            appendLine("${segment.startMs.toTimestamp()} --> ${segment.endMs.toTimestamp()}")
            appendLine(segment.text)
            appendLine()
        }
    }

    fun writeSrt(recording: Recording, output: OutputStream) {
        output.write(renderSrt(recording).toByteArray(Charsets.UTF_8))
    }

    fun writeVtt(recording: Recording, output: OutputStream) {
        output.write(renderVtt(recording).toByteArray(Charsets.UTF_8))
    }
}
