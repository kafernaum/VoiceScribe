package com.yourdomain.voicescribe.feature.onboarding

enum class OnboardingPage { WELCOME, PERMISSION_RATIONALE, MODEL_DOWNLOAD, DONE }

data class OnboardingUiState(
    val page: OnboardingPage = OnboardingPage.WELCOME,
    val microphonePermissionGranted: Boolean = false,
    val modelDownloadProgress: Float = 0f,
    val isModelReady: Boolean = false,
)

sealed interface OnboardingIntent {
    data object NextPage : OnboardingIntent
    data class SetMicrophonePermissionGranted(val granted: Boolean) : OnboardingIntent
    data object StartModelDownload : OnboardingIntent
    data object Finish : OnboardingIntent
}
