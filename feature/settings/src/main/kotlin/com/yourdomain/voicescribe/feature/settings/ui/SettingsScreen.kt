package com.yourdomain.voicescribe.feature.settings.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yourdomain.voicescribe.core.domain.model.SttEngine
import com.yourdomain.voicescribe.feature.settings.SettingsIntent
import com.yourdomain.voicescribe.feature.settings.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = { TopAppBar(title = { Text("Settings") }) }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            item {
                var engineMenuExpanded by remember { mutableStateOf(false) }
                ListItem(
                    headlineContent = { Text("Speech recognition engine") },
                    supportingContent = { Text(uiState.preferredEngine.name) },
                    trailingContent = {
                        Row {
                            TextButton(onClick = { engineMenuExpanded = true }) { Text("Change") }
                            DropdownMenu(expanded = engineMenuExpanded, onDismissRequest = { engineMenuExpanded = false }) {
                                SttEngine.entries.forEach { engine ->
                                    DropdownMenuItem(
                                        text = { Text(engine.name) },
                                        onClick = {
                                            viewModel.onIntent(SettingsIntent.SetEngine(engine))
                                            engineMenuExpanded = false
                                        },
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("Encrypt recordings at rest") },
                    supportingContent = { Text("SQLCipher + Android Keystore") },
                    trailingContent = {
                        Switch(
                            checked = uiState.encryptionEnabled,
                            onCheckedChange = { enabled -> viewModel.onIntent(SettingsIntent.SetEncryptionEnabled(enabled)) },
                        )
                    },
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("Require biometric unlock") },
                    trailingContent = {
                        Switch(
                            checked = uiState.biometricLockEnabled,
                            onCheckedChange = { enabled -> viewModel.onIntent(SettingsIntent.SetBiometricLockEnabled(enabled)) },
                        )
                    },
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("Incognito mode by default") },
                    supportingContent = { Text("Nothing is saved when a recording ends") },
                    trailingContent = {
                        Switch(
                            checked = uiState.incognitoModeDefault,
                            onCheckedChange = { enabled -> viewModel.onIntent(SettingsIntent.SetIncognitoModeDefault(enabled)) },
                        )
                    },
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("Dynamic color (Material You)") },
                    trailingContent = {
                        Switch(
                            checked = uiState.dynamicColorEnabled,
                            onCheckedChange = { enabled -> viewModel.onIntent(SettingsIntent.SetDynamicColorEnabled(enabled)) },
                        )
                    },
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("Auto-delete trash after") },
                    supportingContent = { Text("${uiState.autoDeleteTrashDays} days") },
                )
            }
        }
    }
}
