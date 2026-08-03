package com.yourdomain.voicescribe.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.yourdomain.voicescribe.app.navigation.Destinations
import com.yourdomain.voicescribe.app.navigation.VoiceScribeNavGraph
import org.koin.androidx.compose.koinViewModel

@Composable
fun VoiceScribeApp(appViewModel: AppViewModel = koinViewModel()) {
    val onboardingCompleted by appViewModel.onboardingCompleted.collectAsStateWithLifecycle()
    val completed = onboardingCompleted ?: return // brief first frame while DataStore loads

    val navController = rememberNavController()
    VoiceScribeNavGraph(
        navController = navController,
        startDestination = if (completed) Destinations.RECORDING else Destinations.ONBOARDING,
    )
}
