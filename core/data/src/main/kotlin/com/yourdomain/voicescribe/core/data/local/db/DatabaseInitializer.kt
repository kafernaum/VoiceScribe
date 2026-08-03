package com.yourdomain.voicescribe.core.data.local.db

import android.content.Context
import net.sqlcipher.database.SQLiteDatabase

/**
 * Loads SQLCipher's native libraries. Must be called once, before the first
 * [VoiceScribeDatabase.build] call (VoiceScribeApplication.onCreate does
 * this). Kept inside core:data so `:app` never needs a direct dependency on
 * `net.zetetic:sqlcipher-android` — SQLCipher stays an internal
 * implementation detail of the data layer.
 */
object DatabaseInitializer {
    fun loadNativeLibraries(context: Context) {
        SQLiteDatabase.loadLibs(context.applicationContext)
    }
}
