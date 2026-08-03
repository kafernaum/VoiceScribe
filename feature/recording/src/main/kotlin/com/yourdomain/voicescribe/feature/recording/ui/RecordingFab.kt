package com.yourdomain.voicescribe.feature.recording.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.yourdomain.voicescribe.feature.recording.RecordingUiState

@Composable
fun StartRecordingFab(onClick: () -> Unit) {
    FloatingActionButton(onClick = onClick) {
        Icon(Icons.Filled.Mic, contentDescription = "Start recording")
    }
}

@Composable
fun RecordingFabRow(
    state: RecordingUiState,
    onBookmark: () -> Unit,
    onPauseResume: () -> Unit,
    onStop: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SmallFloatingActionButton(onClick = onBookmark) {
            Icon(Icons.Filled.Bookmark, contentDescription = "Add bookmark")
        }
        FloatingActionButton(onClick = onPauseResume) {
            Icon(
                if (state.isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                contentDescription = if (state.isPaused) "Resume" else "Pause",
            )
        }
        FloatingActionButton(onClick = onStop) {
            Icon(Icons.Filled.Stop, contentDescription = "Stop")
        }
    }
}
