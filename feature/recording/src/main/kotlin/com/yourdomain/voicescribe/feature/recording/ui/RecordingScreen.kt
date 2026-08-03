package com.yourdomain.voicescribe.feature.recording.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yourdomain.voicescribe.core.domain.model.RecordingConfig
import com.yourdomain.voicescribe.feature.recording.RecordingEffect
import com.yourdomain.voicescribe.feature.recording.RecordingIntent
import com.yourdomain.voicescribe.feature.recording.RecordingUiState
import com.yourdomain.voicescribe.feature.recording.RecordingViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * The recording screen: a live waveform/VU meter, a scrolling live
 * transcript, and a FAB whose shape changes with [RecordingUiState.phase]
 * (start -> bookmark/pause/stop).
 */
@Composable
fun RecordingScreen(
    onNavigateToLibrary: () -> Unit,
    viewModel: RecordingViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                RecordingEffect.NavigateToLibrary -> onNavigateToLibrary()
                is RecordingEffect.ShowMessage -> Unit // wired to a SnackbarHostState by the app-level scaffold
            }
        }
    }

    RecordingScreenContent(
        uiState = uiState,
        onIntent = viewModel::onIntent,
    )
}

@Composable
private fun RecordingScreenContent(
    uiState: RecordingUiState,
    onIntent: (RecordingIntent) -> Unit,
) {
    Scaffold(
        topBar = { RecordingTopBar(state = uiState, onClose = { onIntent(RecordingIntent.Close) }) },
        floatingActionButton = {
            if (uiState.phase == RecordingUiState.Phase.RECORDING) {
                RecordingFabRow(
                    state = uiState,
                    onBookmark = { onIntent(RecordingIntent.AddBookmark()) },
                    onPauseResume = { onIntent(RecordingIntent.PauseOrResume) },
                    onStop = { onIntent(RecordingIntent.Stop) },
                )
            } else {
                StartRecordingFab(
                    onClick = { onIntent(RecordingIntent.StartRecording(RecordingConfig())) },
                )
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            AnimatedWaveform(
                audioLevel = uiState.audioLevel,
                isRecording = uiState.phase == RecordingUiState.Phase.RECORDING,
                modifier = Modifier.padding(16.dp),
            )
            Box(modifier = Modifier.fillMaxSize()) {
                LiveTranscriptView(
                    segments = uiState.segments,
                    partialText = uiState.partialText,
                    locale = uiState.config.locale,
                )
            }
        }
    }
}
