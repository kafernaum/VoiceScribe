package com.yourdomain.voicescribe.app

import android.app.Application
import com.yourdomain.voicescribe.app.di.appModule
import com.yourdomain.voicescribe.core.audio.di.audioModule
import com.yourdomain.voicescribe.core.data.di.dataModule
import com.yourdomain.voicescribe.core.data.local.db.DatabaseInitializer
import com.yourdomain.voicescribe.core.domain.di.useCaseModule
import com.yourdomain.voicescribe.feature.library.di.libraryFeatureModule
import com.yourdomain.voicescribe.feature.onboarding.di.onboardingFeatureModule
import com.yourdomain.voicescribe.feature.player.di.playerFeatureModule
import com.yourdomain.voicescribe.feature.recording.di.recordingFeatureModule
import com.yourdomain.voicescribe.feature.settings.di.settingsFeatureModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class VoiceScribeApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Must happen before the first Room/SQLCipher database open.
        DatabaseInitializer.loadNativeLibraries(this)

        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.ERROR else Level.NONE)
            androidContext(this@VoiceScribeApplication)
            modules(
                dataModule,
                audioModule,
                useCaseModule,
                recordingFeatureModule,
                libraryFeatureModule,
                playerFeatureModule,
                settingsFeatureModule,
                onboardingFeatureModule,
                appModule,
            )
        }
    }
}
