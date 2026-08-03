package com.yourdomain.voicescribe.core.data.local.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.yourdomain.voicescribe.core.data.local.db.dao.BookmarkDao
import com.yourdomain.voicescribe.core.data.local.db.dao.RecordingDao
import com.yourdomain.voicescribe.core.data.local.db.dao.TranscriptDao
import com.yourdomain.voicescribe.core.data.local.db.entity.BookmarkEntity
import com.yourdomain.voicescribe.core.data.local.db.entity.RecordingEntity
import com.yourdomain.voicescribe.core.data.local.db.entity.RecordingFtsEntity
import com.yourdomain.voicescribe.core.data.local.db.entity.TranscriptSegmentEntity
import net.sqlcipher.database.SupportFactory

/**
 * The single Room database for the app, encrypted at rest via SQLCipher.
 * [net.sqlcipher.database.SQLiteDatabase.loadLibs] must be called once
 * (VoiceScribeApplication.onCreate) before [build] is invoked.
 */
@Database(
    entities = [
        RecordingEntity::class,
        RecordingFtsEntity::class,
        BookmarkEntity::class,
        TranscriptSegmentEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class VoiceScribeDatabase : RoomDatabase() {
    abstract fun recordingDao(): RecordingDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun transcriptDao(): TranscriptDao

    companion object {
        const val DATABASE_NAME = "voicescribe.db"

        fun build(context: Context, passphrase: ByteArray): VoiceScribeDatabase {
            val factory = SupportFactory(passphrase)
            return Room.databaseBuilder(context.applicationContext, VoiceScribeDatabase::class.java, DATABASE_NAME)
                .openHelperFactory(factory)
                .build()
        }
    }
}
