package com.yourdomain.voicescribe.feature.library.export

import com.yourdomain.voicescribe.core.domain.model.Recording
import java.io.OutputStream

class TxtExporter {
    fun render(recording: Recording): String = buildString {
        appendLine(recording.title)
        appendLine()
        recording.transcriptSegments.filter { it.isFinal }.forEach { segment -> appendLine(segment.text) }
    }

    fun write(recording: Recording, output: OutputStream) {
        output.write(render(recording).toByteArray(Charsets.UTF_8))
    }
}
