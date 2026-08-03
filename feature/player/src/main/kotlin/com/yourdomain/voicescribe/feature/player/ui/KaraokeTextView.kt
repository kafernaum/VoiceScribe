package com.yourdomain.voicescribe.feature.player.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yourdomain.voicescribe.core.domain.model.TranscriptSegment

/**
 * "Karaoke" transcript: auto-scrolls to and highlights the segment
 * containing [currentPositionMs]. Word-level highlighting (rather than
 * segment-level) is a direct extension once every [TranscriptSegment.words]
 * entry is populated by the active STT engine — see STT_ENGINES.md for which
 * engines currently report word timings.
 */
@Composable
fun KaraokeTextView(
    segments: List<TranscriptSegment>,
    currentPositionMs: Long,
    onSeekToSegment: (TranscriptSegment) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val activeIndex = segments.indexOfFirst { currentPositionMs in it.startMs..it.endMs }

    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0) listState.animateScrollToItem(activeIndex)
    }

    LazyColumn(modifier = modifier, state = listState) {
        items(segments, key = { it.id }) { segment ->
            val isActive = segment == segments.getOrNull(activeIndex)
            Text(
                text = segment.text,
                textAlign = TextAlign.Start,
                color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(vertical = 4.dp, horizontal = 16.dp)
                    .then(
                        if (isActive) {
                            Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                        } else {
                            Modifier
                        },
                    )
                    .clickable { onSeekToSegment(segment) }
                    .padding(8.dp),
            )
        }
    }
}
