package com.yourdomain.voicescribe.feature.library.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.yourdomain.voicescribe.core.common.extensions.toTimestamp
import com.yourdomain.voicescribe.core.domain.model.Recording

@Composable
fun RecordingListItem(
    recording: Recording,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier = modifier.fillMaxWidth(),
        headlineContent = { Text(recording.title) },
        supportingContent = {
            Column {
                Text("${recording.durationMs.toTimestamp(includeMillis = false)} • ${recording.language}")
                if (recording.tags.isNotEmpty()) {
                    Text(recording.tags.joinToString(", "))
                }
            }
        },
        trailingContent = {
            Row {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        if (recording.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = "Favorite",
                    )
                }
                IconButton(onClick = onExport) {
                    Icon(Icons.Filled.Share, contentDescription = "Export")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete")
                }
            }
        },
    )
}
