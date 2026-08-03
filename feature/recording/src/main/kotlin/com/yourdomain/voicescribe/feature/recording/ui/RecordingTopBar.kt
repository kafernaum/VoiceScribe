package com.yourdomain.voicescribe.feature.recording.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import com.yourdomain.voicescribe.core.common.extensions.toTimestamp
import com.yourdomain.voicescribe.feature.recording.RecordingUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingTopBar(state: RecordingUiState, onClose: () -> Unit) {
    TopAppBar(
        title = {
            val label = when (state.phase) {
                RecordingUiState.Phase.RECORDING -> if (state.isPaused) "Paused" else "Recording"
                RecordingUiState.Phase.PREPARING -> "Preparing…"
                else -> "VoiceScribe"
            }
            Text("$label  ${state.elapsedMs.toTimestamp(includeMillis = false)}")
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = "Close")
            }
        },
    )
}
