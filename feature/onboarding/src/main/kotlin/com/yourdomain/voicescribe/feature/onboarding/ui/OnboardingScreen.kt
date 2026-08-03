package com.yourdomain.voicescribe.feature.onboarding.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yourdomain.voicescribe.feature.onboarding.OnboardingIntent
import com.yourdomain.voicescribe.feature.onboarding.OnboardingPage
import com.yourdomain.voicescribe.feature.onboarding.OnboardingViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.page) {
        if (uiState.page == OnboardingPage.DONE) {
            viewModel.onIntent(OnboardingIntent.Finish)
            onFinished()
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when (uiState.page) {
                OnboardingPage.WELCOME -> {
                    Text("Welcome to VoiceScribe")
                    Text("Record and transcribe — 100% on your device, nothing in the cloud.")
                    Button(onClick = { viewModel.onIntent(OnboardingIntent.NextPage) }) { Text("Get started") }
                }
                OnboardingPage.PERMISSION_RATIONALE -> {
                    PermissionRationaleScreen(
                        onGranted = {
                            viewModel.onIntent(OnboardingIntent.SetMicrophonePermissionGranted(true))
                            viewModel.onIntent(OnboardingIntent.NextPage)
                        },
                    )
                }
                OnboardingPage.MODEL_DOWNLOAD -> {
                    Text("Preparing the on-device speech model…")
                    LinearProgressIndicator(progress = { uiState.modelDownloadProgress })
                    LaunchedEffect(Unit) { viewModel.onIntent(OnboardingIntent.StartModelDownload) }
                    if (uiState.isModelReady) {
                        Button(onClick = { viewModel.onIntent(OnboardingIntent.NextPage) }) { Text("Continue") }
                    }
                }
                OnboardingPage.DONE -> {
                    Text("All set!")
                }
            }
        }
    }
}
