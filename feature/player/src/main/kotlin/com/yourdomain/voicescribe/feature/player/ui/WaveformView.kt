package com.yourdomain.voicescribe.feature.player.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * Interactive waveform: tap anywhere to seek to that proportional position.
 * Pinch-to-zoom is a natural follow-up (track a zoom scale in
 * `PlaybackState` and window [peaks] by it) — omitted here to keep the
 * gesture handling readable.
 */
@Composable
fun WaveformView(
    peaks: FloatArray,
    progress: Float,
    onSeekToProgress: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val playedColor = MaterialTheme.colorScheme.primary
    val unplayedColor = MaterialTheme.colorScheme.surfaceVariant

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val proportion = (offset.x / size.width).coerceIn(0f, 1f)
                    onSeekToProgress(proportion)
                }
            },
    ) {
        if (peaks.isEmpty()) return@Canvas
        val barWidth = size.width / peaks.size
        val playedBars = (progress.coerceIn(0f, 1f) * peaks.size).toInt()

        peaks.forEachIndexed { index, peak ->
            val barHeight = peak.coerceIn(0f, 1f) * size.height
            val x = index * barWidth
            drawRect(
                color = if (index <= playedBars) playedColor else unplayedColor,
                topLeft = Offset(x, (size.height - barHeight) / 2f),
                size = Size(barWidth * 0.8f, barHeight),
            )
        }
    }
}
