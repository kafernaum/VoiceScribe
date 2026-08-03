package com.yourdomain.voicescribe.feature.onboarding.di

import com.yourdomain.voicescribe.feature.onboarding.OnboardingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val onboardingFeatureModule = module {
    viewModel { OnboardingViewModel(get(), get()) }
}
