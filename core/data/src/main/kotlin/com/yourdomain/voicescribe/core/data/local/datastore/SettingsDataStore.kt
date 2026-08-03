package com.yourdomain.voicescribe.core.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.yourdomain.voicescribe.core.domain.model.AudioQuality
import com.yourdomain.voicescribe.core.domain.model.SttEngine
import com.yourdomain.voicescribe.core.domain.repository.SettingsRepository
import com.yourdomain.voicescribe.core.domain.repository.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "voicescribe_settings")

/** [SettingsRepository] backed by Jetpack DataStore (Preferences). */
class SettingsDataStore(private val dataStore: DataStore<Preferences>) : SettingsRepository {

    override val preferredEngine: Flow<SttEngine> = dataStore.data.map { prefs ->
        prefs[KEY_ENGINE]?.let { runCatching { SttEngine.valueOf(it) }.getOrNull() } ?: SttEngine.ML_KIT_GENAI_AUTO
    }

    override val preferredLocale: Flow<String> = dataStore.data.map { prefs -> prefs[KEY_LOCALE] ?: "en-US" }

    override val audioQuality: Flow<AudioQuality> = dataStore.data.map { prefs ->
        prefs[KEY_QUALITY]?.let { runCatching { AudioQuality.valueOf(it) }.getOrNull() } ?: AudioQuality.VOICE_STREAMING
    }

    override val encryptionEnabled: Flow<Boolean> = dataStore.data.map { prefs -> prefs[KEY_ENCRYPTION] ?: true }
    override val biometricLockEnabled: Flow<Boolean> = dataStore.data.map { prefs -> prefs[KEY_BIOMETRIC_LOCK] ?: false }
    override val autoDeleteTrashDays: Flow<Int> = dataStore.data.map { prefs -> prefs[KEY_AUTO_DELETE_DAYS] ?: 30 }
    override val incognitoModeDefault: Flow<Boolean> = dataStore.data.map { prefs -> prefs[KEY_INCOGNITO] ?: false }

    override val darkThemeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        prefs[KEY_THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM
    }

    override val dynamicColorEnabled: Flow<Boolean> = dataStore.data.map { prefs -> prefs[KEY_DYNAMIC_COLOR] ?: true }
    override val onboardingCompleted: Flow<Boolean> = dataStore.data.map { prefs -> prefs[KEY_ONBOARDING_COMPLETED] ?: false }

    override suspend fun setPreferredEngine(engine: SttEngine) {
        dataStore.edit { it[KEY_ENGINE] = engine.name }
    }

    override suspend fun setPreferredLocale(locale: String) {
        dataStore.edit { it[KEY_LOCALE] = locale }
    }

    override suspend fun setAudioQuality(quality: AudioQuality) {
        dataStore.edit { it[KEY_QUALITY] = quality.name }
    }

    override suspend fun setEncryptionEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_ENCRYPTION] = enabled }
    }

    override suspend fun setBiometricLockEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_BIOMETRIC_LOCK] = enabled }
    }

    override suspend fun setAutoDeleteTrashDays(days: Int) {
        dataStore.edit { it[KEY_AUTO_DELETE_DAYS] = days }
    }

    override suspend fun setIncognitoModeDefault(enabled: Boolean) {
        dataStore.edit { it[KEY_INCOGNITO] = enabled }
    }

    override suspend fun setDarkThemeMode(mode: ThemeMode) {
        dataStore.edit { it[KEY_THEME] = mode.name }
    }

    override suspend fun setDynamicColorEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_DYNAMIC_COLOR] = enabled }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[KEY_ONBOARDING_COMPLETED] = completed }
    }

    companion object {
        private val KEY_ENGINE = stringPreferencesKey("preferred_engine")
        private val KEY_LOCALE = stringPreferencesKey("preferred_locale")
        private val KEY_QUALITY = stringPreferencesKey("audio_quality")
        private val KEY_ENCRYPTION = booleanPreferencesKey("encryption_enabled")
        private val KEY_BIOMETRIC_LOCK = booleanPreferencesKey("biometric_lock_enabled")
        private val KEY_AUTO_DELETE_DAYS = intPreferencesKey("auto_delete_trash_days")
        private val KEY_INCOGNITO = booleanPreferencesKey("incognito_mode_default")
        private val KEY_THEME = stringPreferencesKey("dark_theme_mode")
        private val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color_enabled")
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }
}
