package com.yourdomain.voicescribe.core.domain.repository

import com.yourdomain.voicescribe.core.domain.model.AudioQuality
import com.yourdomain.voicescribe.core.domain.model.SttEngine
import kotlinx.coroutines.flow.Flow

/** User preferences, persisted via DataStore (implemented in `core:data`). */
interface SettingsRepository {
    val preferredEngine: Flow<SttEngine>
    val preferredLocale: Flow<String>
    val audioQuality: Flow<AudioQuality>
    val encryptionEnabled: Flow<Boolean>
    val biometricLockEnabled: Flow<Boolean>
    val autoDeleteTrashDays: Flow<Int>
    val incognitoModeDefault: Flow<Boolean>
    val darkThemeMode: Flow<ThemeMode>
    val dynamicColorEnabled: Flow<Boolean>
    val onboardingCompleted: Flow<Boolean>

    suspend fun setPreferredEngine(engine: SttEngine)
    suspend fun setPreferredLocale(locale: String)
    suspend fun setAudioQuality(quality: AudioQuality)
    suspend fun setEncryptionEnabled(enabled: Boolean)
    suspend fun setBiometricLockEnabled(enabled: Boolean)
    suspend fun setAutoDeleteTrashDays(days: Int)
    suspend fun setIncognitoModeDefault(enabled: Boolean)
    suspend fun setDarkThemeMode(mode: ThemeMode)
    suspend fun setDynamicColorEnabled(enabled: Boolean)
    suspend fun setOnboardingCompleted(completed: Boolean)
}

enum class ThemeMode { LIGHT, DARK, SYSTEM }
