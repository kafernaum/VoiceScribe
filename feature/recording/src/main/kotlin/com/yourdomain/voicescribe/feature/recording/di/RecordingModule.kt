package com.yourdomain.voicescribe.feature.recording.di

import com.yourdomain.voicescribe.feature.recording.RecordingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val recordingFeatureModule = module {
    viewModel { RecordingViewModel(get(), get(), get(), get()) }
}
