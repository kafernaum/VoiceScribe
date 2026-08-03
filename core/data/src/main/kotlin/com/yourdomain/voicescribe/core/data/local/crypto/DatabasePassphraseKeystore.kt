package com.yourdomain.voicescribe.core.data.local.crypto

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

/**
 * Generates and stores the random 256-bit passphrase used to open the
 * SQLCipher-encrypted Room database. The passphrase itself lives inside
 * [EncryptedSharedPreferences], whose key is protected by the Android
 * Keystore (StrongBox-backed on supported devices) — so the passphrase never
 * touches disk in plaintext.
 *
 * When the user enables the "biometric lock" setting (feature:settings),
 * the app additionally gates *unlocking the app UI* behind BiometricPrompt;
 * that is a separate, app-level gate layered on top of this at-rest
 * encryption and is implemented in feature:onboarding/feature:settings.
 */
class DatabasePassphraseKeystore(context: Context) {

    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context.applicationContext,
        PREFS_FILE_NAME,
        MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    fun getOrCreatePassphrase(): ByteArray {
        encryptedPrefs.getString(KEY_DB_PASSPHRASE, null)?.let { encoded ->
            return Base64.decode(encoded, Base64.NO_WRAP)
        }
        val newPassphrase = ByteArray(PASSPHRASE_BYTES).also(SecureRandom()::nextBytes)
        encryptedPrefs.edit()
            .putString(KEY_DB_PASSPHRASE, Base64.encodeToString(newPassphrase, Base64.NO_WRAP))
            .apply()
        return newPassphrase
    }

    companion object {
        private const val PREFS_FILE_NAME = "voicescribe_secure_prefs"
        private const val KEY_DB_PASSPHRASE = "db_passphrase"
        private const val PASSPHRASE_BYTES = 32
    }
}
