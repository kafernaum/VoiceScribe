package com.yourdomain.voicescribe.core.data.di

import com.yourdomain.voicescribe.core.data.local.crypto.DatabasePassphraseKeystore
import com.yourdomain.voicescribe.core.data.local.datastore.SettingsDataStore
import com.yourdomain.voicescribe.core.data.local.datastore.settingsDataStore
import com.yourdomain.voicescribe.core.data.local.db.VoiceScribeDatabase
import com.yourdomain.voicescribe.core.data.repository.RecordingRepositoryImpl
import com.yourdomain.voicescribe.core.domain.repository.RecordingRepository
import com.yourdomain.voicescribe.core.domain.repository.SettingsRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single { DatabasePassphraseKeystore(androidContext()) }

    single {
        VoiceScribeDatabase.build(
            context = androidContext(),
            passphrase = get<DatabasePassphraseKeystore>().getOrCreatePassphrase(),
        )
    }

    single { get<VoiceScribeDatabase>().recordingDao() }
    single { get<VoiceScribeDatabase>().bookmarkDao() }
    single { get<VoiceScribeDatabase>().transcriptDao() }

    single<RecordingRepository> { RecordingRepositoryImpl(get(), get(), get()) }

    single { androidContext().settingsDataStore }
    single<SettingsRepository> { SettingsDataStore(get()) }
}
