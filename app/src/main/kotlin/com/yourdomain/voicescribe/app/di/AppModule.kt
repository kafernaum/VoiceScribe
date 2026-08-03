package com.yourdomain.voicescribe.app.di

import com.yourdomain.voicescribe.app.AppViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { AppViewModel(get()) }
}
