package com.yourdomain.voicescribe.feature.library.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yourdomain.voicescribe.core.domain.model.LibraryFilter

@Composable
fun FilterBar(
    filter: LibraryFilter,
    onQueryChange: (String) -> Unit,
    onFavoritesOnlyToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        OutlinedTextField(
            value = filter.query,
            onValueChange = onQueryChange,
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            placeholder = { Text("Search recordings and transcripts") },
            singleLine = true,
            modifier = Modifier,
        )
    }
    LazyRow(modifier = Modifier.padding(horizontal = 16.dp)) {
        item {
            FilterChip(
                selected = filter.favoritesOnly,
                onClick = { onFavoritesOnlyToggle(!filter.favoritesOnly) },
                label = { Text("Favorites") },
            )
        }
    }
}
