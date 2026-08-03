package com.yourdomain.voicescribe.feature.library.export

import com.yourdomain.voicescribe.core.domain.model.Recording
import com.yourdomain.voicescribe.core.domain.model.TranscriptSegment
import com.yourdomain.voicescribe.core.domain.model.WordTiming
import java.io.OutputStream

/**
 * Hand-rolled JSON writer to avoid adding a serialization dependency for
 * what is a small, fixed export shape. If the schema grows materially,
 * switch to kotlinx-serialization.
 */
class JsonExporter {

    fun render(recording: Recording): String {
        val finalSegments = recording.transcriptSegments.filter { it.isFinal }
        val segmentsJson = finalSegments.joinToString(",\n") { segmentToJson(it) }
        return buildString {
            appendLine("{")
            appendLine("  \"id\": \"${esc(recording.id)}\",")
            appendLine("  \"title\": \"${esc(recording.title)}\",")
            appendLine("  \"language\": \"${esc(recording.language)}\",")
            appendLine("  \"durationMs\": ${recording.durationMs},")
            appendLine("  \"engine\": \"${recording.engine.name}\",")
            appendLine("  \"wordCount\": ${recording.wordCount},")
            appendLine("  \"segments\": [")
            append(segmentsJson)
            appendLine()
            appendLine("  ]")
            append("}")
        }
    }

    fun write(recording: Recording, output: OutputStream) {
        output.write(render(recording).toByteArray(Charsets.UTF_8))
    }

    private fun segmentToJson(segment: TranscriptSegment): String {
        val wordsJson = segment.words.joinToString(",") { wordToJson(it) }
        val speakerJson = segment.speaker?.let { "\"${esc(it)}\"" } ?: "null"
        val confidenceJson = segment.confidence?.toString() ?: "null"
        return "    {\"startMs\":${segment.startMs},\"endMs\":${segment.endMs}," +
            "\"text\":\"${esc(segment.text)}\",\"confidence\":$confidenceJson," +
            "\"speaker\":$speakerJson,\"words\":[$wordsJson]}"
    }

    private fun wordToJson(word: WordTiming): String {
        val confidenceJson = word.confidence?.toString() ?: "null"
        return "{\"text\":\"${esc(word.text)}\",\"startMs\":${word.startMs}," +
            "\"endMs\":${word.endMs},\"confidence\":$confidenceJson}"
    }

    private fun esc(value: String): String = value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "")
}
