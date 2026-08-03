package com.yourdomain.voicescribe.feature.player.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yourdomain.voicescribe.core.common.extensions.toTimestamp
import com.yourdomain.voicescribe.feature.player.AVAILABLE_PLAYBACK_SPEEDS
import com.yourdomain.voicescribe.feature.player.PlayerIntent
import com.yourdomain.voicescribe.feature.player.PlayerViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun PlayerScreen(
    recordingId: String,
    viewModel: PlayerViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(recordingId) {
        viewModel.onIntent(PlayerIntent.Load(recordingId))
    }

    val recording = uiState.recording
    val playback = uiState.playback
    var speedMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(recording?.title ?: "Player") }) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            val durationMs = recording?.durationMs?.coerceAtLeast(1L) ?: 1L
            val progress = (playback.currentPositionMs.toFloat() / durationMs).coerceIn(0f, 1f)

            WaveformView(
                peaks = playback.waveformPeaks,
                progress = progress,
                onSeekToProgress = { proportion ->
                    viewModel.onIntent(PlayerIntent.SeekTo((proportion * durationMs).toLong()))
                },
                modifier = Modifier.padding(16.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            ) {
                Text("${playback.currentPositionMs.toTimestamp(includeMillis = false)} / ${durationMs.toTimestamp(includeMillis = false)}")
            }

            Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                IconButton(onClick = { viewModel.onIntent(PlayerIntent.PlayPause) }) {
                    Icon(
                        if (playback.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (playback.isPlaying) "Pause" else "Play",
                    )
                }

                TextButton(onClick = { speedMenuExpanded = true }) {
                    Text("${playback.speed}x")
                }
                DropdownMenu(expanded = speedMenuExpanded, onDismissRequest = { speedMenuExpanded = false }) {
                    AVAILABLE_PLAYBACK_SPEEDS.forEach { speed ->
                        DropdownMenuItem(
                            text = { Text("${speed}x") },
                            onClick = {
                                viewModel.onIntent(PlayerIntent.SetSpeed(speed))
                                speedMenuExpanded = false
                            },
                        )
                    }
                }

                Text("Skip silence")
                Switch(
                    checked = playback.skipSilenceEnabled,
                    onCheckedChange = { enabled -> viewModel.onIntent(PlayerIntent.SetSkipSilence(enabled)) },
                )

                IconButton(onClick = { viewModel.onIntent(PlayerIntent.AddBookmark()) }) {
                    Icon(Icons.Filled.Bookmark, contentDescription = "Add bookmark")
                }
            }

            KaraokeTextView(
                segments = recording?.transcriptSegments.orEmpty(),
                currentPositionMs = playback.currentPositionMs,
                onSeekToSegment = { segment -> viewModel.onIntent(PlayerIntent.SeekTo(segment.startMs)) },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
