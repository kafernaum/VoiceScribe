package com.yourdomain.voicescribe.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourdomain.voicescribe.core.domain.port.SpeechRecognizerProvider
import com.yourdomain.voicescribe.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val speechRecognizerProvider: SpeechRecognizerProvider,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onIntent(intent: OnboardingIntent) {
        when (intent) {
            OnboardingIntent.NextPage -> advancePage()
            is OnboardingIntent.SetMicrophonePermissionGranted ->
                _uiState.update { it.copy(microphonePermissionGranted = intent.granted) }
            OnboardingIntent.StartModelDownload -> startModelDownload()
            OnboardingIntent.Finish -> finish()
        }
    }

    private fun advancePage() {
        _uiState.update { current ->
            val nextPage = when (current.page) {
                OnboardingPage.WELCOME -> OnboardingPage.PERMISSION_RATIONALE
                OnboardingPage.PERMISSION_RATIONALE -> OnboardingPage.MODEL_DOWNLOAD
                OnboardingPage.MODEL_DOWNLOAD -> OnboardingPage.DONE
                OnboardingPage.DONE -> OnboardingPage.DONE
            }
            current.copy(page = nextPage)
        }
    }

    private fun startModelDownload() {
        viewModelScope.launch {
            val locale = settingsRepository.preferredLocale.first()
            val engine = settingsRepository.preferredEngine.first()
            val session = speechRecognizerProvider.getSession(locale, engine)
            session.ensureModelDownloaded().collect { progress ->
                _uiState.update { it.copy(modelDownloadProgress = progress, isModelReady = progress >= 1f) }
            }
        }
    }

    private fun finish() {
        viewModelScope.launch { settingsRepository.setOnboardingCompleted(true) }
    }
}
