package com.yourdomain.voicescribe.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourdomain.voicescribe.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Decides the nav graph's start destination once the onboarding flag has loaded. */
class AppViewModel(settingsRepository: SettingsRepository) : ViewModel() {
    val onboardingCompleted: StateFlow<Boolean?> = settingsRepository.onboardingCompleted
        .map<Boolean, Boolean?> { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
