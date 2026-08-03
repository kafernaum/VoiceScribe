package com.yourdomain.voicescribe.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourdomain.voicescribe.core.domain.model.AudioQuality
import com.yourdomain.voicescribe.core.domain.model.SttEngine
import com.yourdomain.voicescribe.core.domain.repository.SettingsRepository
import com.yourdomain.voicescribe.core.domain.repository.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Thin pass-through over [SettingsRepository]. Unlike the recording/library
 * ViewModels, this one talks to the repository port directly rather than via
 * a use case — there's no business rule beyond "read/write a preference",
 * so an extra use case per setting would be pure ceremony.
 */
class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    private data class CoreSettings(
        val engine: SttEngine,
        val locale: String,
        val quality: AudioQuality,
        val encryptionEnabled: Boolean,
        val biometricLockEnabled: Boolean,
    )

    private data class ExtraSettings(
        val autoDeleteTrashDays: Int,
        val incognitoModeDefault: Boolean,
        val darkThemeMode: ThemeMode,
        val dynamicColorEnabled: Boolean,
    )

    private val coreSettings = combine(
        settingsRepository.preferredEngine,
        settingsRepository.preferredLocale,
        settingsRepository.audioQuality,
        settingsRepository.encryptionEnabled,
        settingsRepository.biometricLockEnabled,
    ) { engine, locale, quality, encryptionEnabled, biometricLockEnabled ->
        CoreSettings(engine, locale, quality, encryptionEnabled, biometricLockEnabled)
    }

    private val extraSettings = combine(
        settingsRepository.autoDeleteTrashDays,
        settingsRepository.incognitoModeDefault,
        settingsRepository.darkThemeMode,
        settingsRepository.dynamicColorEnabled,
    ) { autoDeleteTrashDays, incognitoModeDefault, darkThemeMode, dynamicColorEnabled ->
        ExtraSettings(autoDeleteTrashDays, incognitoModeDefault, darkThemeMode, dynamicColorEnabled)
    }

    val uiState: StateFlow<SettingsUiState> = combine(coreSettings, extraSettings) { core, extra ->
        SettingsUiState(
            preferredEngine = core.engine,
            preferredLocale = core.locale,
            audioQuality = core.quality,
            encryptionEnabled = core.encryptionEnabled,
            biometricLockEnabled = core.biometricLockEnabled,
            autoDeleteTrashDays = extra.autoDeleteTrashDays,
            incognitoModeDefault = extra.incognitoModeDefault,
            darkThemeMode = extra.darkThemeMode,
            dynamicColorEnabled = extra.dynamicColorEnabled,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun onIntent(intent: SettingsIntent) {
        viewModelScope.launch {
            when (intent) {
                is SettingsIntent.SetEngine -> settingsRepository.setPreferredEngine(intent.engine)
                is SettingsIntent.SetLocale -> settingsRepository.setPreferredLocale(intent.locale)
                is SettingsIntent.SetAudioQuality -> settingsRepository.setAudioQuality(intent.quality)
                is SettingsIntent.SetEncryptionEnabled -> settingsRepository.setEncryptionEnabled(intent.enabled)
                is SettingsIntent.SetBiometricLockEnabled -> settingsRepository.setBiometricLockEnabled(intent.enabled)
                is SettingsIntent.SetAutoDeleteTrashDays -> settingsRepository.setAutoDeleteTrashDays(intent.days)
                is SettingsIntent.SetIncognitoModeDefault -> settingsRepository.setIncognitoModeDefault(intent.enabled)
                is SettingsIntent.SetDarkThemeMode -> settingsRepository.setDarkThemeMode(intent.mode)
                is SettingsIntent.SetDynamicColorEnabled -> settingsRepository.setDynamicColorEnabled(intent.enabled)
            }
        }
    }
}
