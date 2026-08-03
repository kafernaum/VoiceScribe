package com.yourdomain.voicescribe.feature.settings

import com.yourdomain.voicescribe.core.domain.model.AudioQuality
import com.yourdomain.voicescribe.core.domain.model.SttEngine
import com.yourdomain.voicescribe.core.domain.repository.ThemeMode

data class SettingsUiState(
    val preferredEngine: SttEngine = SttEngine.ML_KIT_GENAI_AUTO,
    val preferredLocale: String = "en-US",
    val audioQuality: AudioQuality = AudioQuality.VOICE_STREAMING,
    val encryptionEnabled: Boolean = true,
    val biometricLockEnabled: Boolean = false,
    val autoDeleteTrashDays: Int = 30,
    val incognitoModeDefault: Boolean = false,
    val darkThemeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColorEnabled: Boolean = true,
)

sealed interface SettingsIntent {
    data class SetEngine(val engine: SttEngine) : SettingsIntent
    data class SetLocale(val locale: String) : SettingsIntent
    data class SetAudioQuality(val quality: AudioQuality) : SettingsIntent
    data class SetEncryptionEnabled(val enabled: Boolean) : SettingsIntent
    data class SetBiometricLockEnabled(val enabled: Boolean) : SettingsIntent
    data class SetAutoDeleteTrashDays(val days: Int) : SettingsIntent
    data class SetIncognitoModeDefault(val enabled: Boolean) : SettingsIntent
    data class SetDarkThemeMode(val mode: ThemeMode) : SettingsIntent
    data class SetDynamicColorEnabled(val enabled: Boolean) : SettingsIntent
}
