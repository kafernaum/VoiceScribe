package com.yourdomain.voicescribe.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.yourdomain.voicescribe.app.di.appModule
import com.yourdomain.voicescribe.core.audio.di.audioModule
import com.yourdomain.voicescribe.core.common.AppResult
import com.yourdomain.voicescribe.core.domain.di.useCaseModule
import com.yourdomain.voicescribe.core.domain.model.AudioQuality
import com.yourdomain.voicescribe.core.domain.model.Bookmark
import com.yourdomain.voicescribe.core.domain.model.LibraryFilter
import com.yourdomain.voicescribe.core.domain.model.Recording
import com.yourdomain.voicescribe.core.domain.model.SttEngine
import com.yourdomain.voicescribe.core.domain.model.TranscriptSegment
import com.yourdomain.voicescribe.core.domain.repository.RecordingRepository
import com.yourdomain.voicescribe.core.domain.repository.SettingsRepository
import com.yourdomain.voicescribe.core.domain.repository.ThemeMode
import com.yourdomain.voicescribe.core.domain.usecase.StartRecordingUseCase
import com.yourdomain.voicescribe.feature.library.LibraryViewModel
import com.yourdomain.voicescribe.feature.library.di.libraryFeatureModule
import com.yourdomain.voicescribe.feature.onboarding.OnboardingViewModel
import com.yourdomain.voicescribe.feature.onboarding.di.onboardingFeatureModule
import com.yourdomain.voicescribe.feature.recording.RecordingViewModel
import com.yourdomain.voicescribe.feature.recording.di.recordingFeatureModule
import com.yourdomain.voicescribe.feature.settings.SettingsViewModel
import com.yourdomain.voicescribe.feature.settings.di.settingsFeatureModule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import org.robolectric.RobolectricTestRunner

/**
 * Builds one Koin instance from every module [VoiceScribeApplication]
 * registers (minus `dataModule`, replaced below by in-memory fakes so this
 * test doesn't need SQLCipher's native libraries under Robolectric), then
 * resolves one instance of each layer's key type. A missing binding or a
 * constructor that throws fails this test immediately instead of only
 * surfacing at runtime on a device — the single highest-value test for a
 * DI-heavy, multi-module app like this one.
 *
 * `feature:player`'s `PlayerViewModel` is intentionally not resolved here:
 * it constructs a real `ExoPlayer`, which needs a dedicated Robolectric
 * shadow this smoke test doesn't set up; it's covered by
 * feature:player's own instrumented tests instead.
 */
@RunWith(RobolectricTestRunner::class)
class KoinModuleSmokeTest : KoinTest {

    private class FakeRecordingRepository : RecordingRepository {
        override fun observeRecording(id: String): Flow<Recording?> = flowOf(null)
        override fun observeRecordings(filter: LibraryFilter): Flow<List<Recording>> = flowOf(emptyList())
        override suspend fun insert(recording: Recording) = AppResult.Success(Unit)
        override suspend fun update(recording: Recording) = AppResult.Success(Unit)
        override suspend fun updateFilePathAndSize(id: String, filePath: String, sizeBytes: Long) = AppResult.Success(Unit)
        override suspend fun appendOrUpdateSegment(recordingId: String, segment: TranscriptSegment) = AppResult.Success(Unit)
        override suspend fun replaceSegments(recordingId: String, segments: List<TranscriptSegment>) = AppResult.Success(Unit)
        override suspend fun addBookmark(bookmark: Bookmark) = AppResult.Success(Unit)
        override suspend fun removeBookmark(bookmarkId: String) = AppResult.Success(Unit)
        override suspend fun setFavorite(id: String, isFavorite: Boolean) = AppResult.Success(Unit)
        override suspend fun setTags(id: String, tags: List<String>) = AppResult.Success(Unit)
        override suspend fun setEncrypted(id: String, isEncrypted: Boolean) = AppResult.Success(Unit)
        override suspend fun moveToTrash(id: String, nowEpochMs: Long) = AppResult.Success(Unit)
        override suspend fun restoreFromTrash(id: String) = AppResult.Success(Unit)
        override suspend fun deletePermanently(id: String) = AppResult.Success(Unit)
        override suspend fun purgeTrashOlderThan(cutoffEpochMs: Long) = AppResult.Success(0)
    }

    private class FakeSettingsRepository : SettingsRepository {
        override val preferredEngine: Flow<SttEngine> = flowOf(SttEngine.ML_KIT_GENAI_AUTO)
        override val preferredLocale: Flow<String> = flowOf("en-US")
        override val audioQuality: Flow<AudioQuality> = flowOf(AudioQuality.VOICE_STREAMING)
        override val encryptionEnabled: Flow<Boolean> = flowOf(true)
        override val biometricLockEnabled: Flow<Boolean> = flowOf(false)
        override val autoDeleteTrashDays: Flow<Int> = flowOf(30)
        override val incognitoModeDefault: Flow<Boolean> = flowOf(false)
        override val darkThemeMode: Flow<ThemeMode> = flowOf(ThemeMode.SYSTEM)
        override val dynamicColorEnabled: Flow<Boolean> = flowOf(true)
        override val onboardingCompleted: Flow<Boolean> = flowOf(true)

        override suspend fun setPreferredEngine(engine: SttEngine) = Unit
        override suspend fun setPreferredLocale(locale: String) = Unit
        override suspend fun setAudioQuality(quality: AudioQuality) = Unit
        override suspend fun setEncryptionEnabled(enabled: Boolean) = Unit
        override suspend fun setBiometricLockEnabled(enabled: Boolean) = Unit
        override suspend fun setAutoDeleteTrashDays(days: Int) = Unit
        override suspend fun setIncognitoModeDefault(enabled: Boolean) = Unit
        override suspend fun setDarkThemeMode(mode: ThemeMode) = Unit
        override suspend fun setDynamicColorEnabled(enabled: Boolean) = Unit
        override suspend fun setOnboardingCompleted(completed: Boolean) = Unit
    }

    private val fakeDataModule = module {
        single<RecordingRepository> { FakeRecordingRepository() }
        single<SettingsRepository> { FakeSettingsRepository() }
    }

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        startKoin {
            androidContext(context)
            modules(
                fakeDataModule,
                audioModule,
                useCaseModule,
                recordingFeatureModule,
                libraryFeatureModule,
                settingsFeatureModule,
                onboardingFeatureModule,
                appModule,
            )
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `every non-database layer resolves from the composition root`() {
        get<StartRecordingUseCase>()
        get<RecordingViewModel>()
        get<LibraryViewModel>()
        get<SettingsViewModel>()
        get<OnboardingViewModel>()
    }
}
