package com.yourdomain.voicescribe.core.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.yourdomain.voicescribe.core.data.local.db.VoiceScribeDatabase
import com.yourdomain.voicescribe.core.domain.model.LibraryFilter
import com.yourdomain.voicescribe.core.domain.model.Recording
import com.yourdomain.voicescribe.core.domain.model.RecordingMode
import com.yourdomain.voicescribe.core.domain.model.SttEngine
import com.yourdomain.voicescribe.core.domain.model.TranscriptSegment
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Uses Room's in-memory builder directly (no SQLCipher `SupportFactory`) so
 * this test runs in a plain JVM/Robolectric process without needing
 * SQLCipher's native libraries — production code always goes through
 * [VoiceScribeDatabase.build], which *does* apply encryption.
 */
@RunWith(RobolectricTestRunner::class)
class RecordingRepositoryImplTest {

    private lateinit var database: VoiceScribeDatabase
    private lateinit var repository: RecordingRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, VoiceScribeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = RecordingRepositoryImpl(database.recordingDao(), database.bookmarkDao(), database.transcriptDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun sampleRecording(id: String, title: String) = Recording(
        id = id,
        title = title,
        filePath = "/tmp/$id.opus",
        durationMs = 60_000,
        sizeBytes = 500_000,
        createdAtEpochMs = 1_000L,
        language = "en-US",
        engine = SttEngine.SYSTEM_ON_DEVICE,
        mode = RecordingMode.STREAMING,
        isEncrypted = true,
    )

    @Test
    fun `insert then observeRecordings returns the recording`() = runBlocking {
        repository.insert(sampleRecording("rec-1", "Team standup"))

        val results = repository.observeRecordings(LibraryFilter()).first()

        assertEquals(1, results.size)
        assertEquals("Team standup", results.first().title)
    }

    @Test
    fun `moveToTrash excludes the recording from the default filter`() = runBlocking {
        repository.insert(sampleRecording("rec-2", "Sprint planning"))
        repository.moveToTrash("rec-2", nowEpochMs = 2_000L)

        val visible = repository.observeRecordings(LibraryFilter()).first()
        val includingTrash = repository.observeRecordings(LibraryFilter(includeTrashed = true)).first()

        assertEquals(0, visible.size)
        assertEquals(1, includingTrash.size)
    }

    @Test
    fun `appendOrUpdateSegment refreshes searchable text so FTS can find it`() = runBlocking {
        repository.insert(sampleRecording("rec-3", "Design review"))
        repository.appendOrUpdateSegment(
            "rec-3",
            TranscriptSegment(id = "seg-1", startMs = 0, endMs = 1_000, text = "unique keyword mentioned here", confidence = 0.95f, isFinal = true),
        )

        val results = repository.observeRecordings(LibraryFilter(query = "keyword")).first()

        assertEquals(1, results.size)
        assertEquals("rec-3", results.first().id)
    }
}
