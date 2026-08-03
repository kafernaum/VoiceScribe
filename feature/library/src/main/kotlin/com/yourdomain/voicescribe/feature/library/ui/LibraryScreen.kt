package com.yourdomain.voicescribe.feature.library.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yourdomain.voicescribe.core.common.Constants
import com.yourdomain.voicescribe.core.domain.model.ExportFormat
import com.yourdomain.voicescribe.feature.library.LibraryEffect
import com.yourdomain.voicescribe.feature.library.LibraryIntent
import com.yourdomain.voicescribe.feature.library.LibraryViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun LibraryScreen(
    onOpenRecording: (String) -> Unit,
    viewModel: LibraryViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var pendingExport by remember { mutableStateOf<Pair<String, ExportFormat>?>(null) }
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*"),
    ) { uri ->
        val export = pendingExport
        pendingExport = null
        if (uri != null && export != null) {
            viewModel.onIntent(LibraryIntent.Export(export.first, export.second, uri.toString()))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is LibraryEffect.ShowMessage -> coroutineScope.launch { snackbarHostState.showSnackbar(effect.message) }
                is LibraryEffect.RequestExportDestination -> {
                    pendingExport = effect.recordingId to effect.format
                    createDocumentLauncher.launch(defaultFileName(effect.format))
                }
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Library") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(modifier = androidx.compose.ui.Modifier.padding(padding)) {
            FilterBar(
                filter = uiState.filter,
                onQueryChange = { query -> viewModel.onIntent(LibraryIntent.Search(query)) },
                onFavoritesOnlyToggle = { enabled -> viewModel.onIntent(LibraryIntent.ToggleFavoritesOnly(enabled)) },
            )
            LazyColumn {
                items(uiState.recordings, key = { it.id }) { recording ->
                    RecordingListItem(
                        recording = recording,
                        onClick = { onOpenRecording(recording.id) },
                        onToggleFavorite = {
                            viewModel.onIntent(LibraryIntent.ToggleFavorite(recording.id, !recording.isFavorite))
                        },
                        onExport = {
                            pendingExport = recording.id to ExportFormat.TXT
                            createDocumentLauncher.launch(defaultFileName(ExportFormat.TXT))
                        },
                        onDelete = { viewModel.onIntent(LibraryIntent.MoveToTrash(recording.id)) },
                    )
                }
            }
        }
    }
}

private fun defaultFileName(format: ExportFormat): String = when (format) {
    ExportFormat.TXT -> "transcript.txt"
    ExportFormat.SRT -> "transcript.srt"
    ExportFormat.VTT -> "transcript.vtt"
    ExportFormat.JSON -> "transcript.json"
    ExportFormat.DOCX -> "transcript.docx"
    ExportFormat.PDF -> "transcript.pdf"
    ExportFormat.ZIP_BUNDLE -> "recording_bundle.zip"
}
