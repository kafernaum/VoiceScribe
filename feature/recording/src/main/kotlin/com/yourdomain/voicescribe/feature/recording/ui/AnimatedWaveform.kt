package com.yourdomain.voicescribe.feature.recording.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme

private const val MAX_BARS = 64

/**
 * A rolling bar-style VU meter driven by [audioLevel] (normalized RMS in
 * [0f, 1f]). Keeps its own bounded history so the caller only needs to push
 * the latest sample each recomposition.
 */
@Composable
fun AnimatedWaveform(audioLevel: Float, isRecording: Boolean, modifier: Modifier = Modifier) {
    val levels = remember { mutableStateListOf<Float>() }

    LaunchedEffect(audioLevel) {
        if (isRecording) {
            levels.add(audioLevel)
            if (levels.size > MAX_BARS) levels.removeAt(0)
        }
    }

    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.surfaceVariant

    Canvas(modifier = modifier.fillMaxWidth().height(96.dp)) {
        if (levels.isEmpty()) return@Canvas
        val barWidth = size.width / MAX_BARS
        levels.forEachIndexed { index, level ->
            val barHeight = (level.coerceIn(0f, 1f)) * size.height
            val x = index * barWidth
            drawRect(
                color = if (isRecording) activeColor else inactiveColor,
                topLeft = androidx.compose.ui.geometry.Offset(x, size.height - barHeight),
                size = androidx.compose.ui.geometry.Size(barWidth * 0.7f, barHeight),
            )
        }
    }
}
