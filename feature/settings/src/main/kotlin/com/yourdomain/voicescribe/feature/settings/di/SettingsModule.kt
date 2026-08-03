package com.yourdomain.voicescribe.feature.settings.di

import com.yourdomain.voicescribe.feature.settings.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val settingsFeatureModule = module {
    viewModel { SettingsViewModel(get()) }
}
