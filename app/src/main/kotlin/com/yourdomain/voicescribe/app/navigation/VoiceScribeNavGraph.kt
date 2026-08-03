package com.yourdomain.voicescribe.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.yourdomain.voicescribe.feature.library.ui.LibraryScreen
import com.yourdomain.voicescribe.feature.onboarding.ui.OnboardingScreen
import com.yourdomain.voicescribe.feature.player.ui.PlayerScreen
import com.yourdomain.voicescribe.feature.recording.ui.RecordingScreen
import com.yourdomain.voicescribe.feature.settings.ui.SettingsScreen

@Composable
fun VoiceScribeNavGraph(navController: NavHostController, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Destinations.ONBOARDING) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Destinations.RECORDING) {
                        popUpTo(Destinations.ONBOARDING) { inclusive = true }
                    }
                },
            )
        }

        composable(Destinations.RECORDING) {
            RecordingScreen(onNavigateToLibrary = { navController.navigate(Destinations.LIBRARY) })
        }

        composable(Destinations.LIBRARY) {
            LibraryScreen(onOpenRecording = { recordingId -> navController.navigate(Destinations.player(recordingId)) })
        }

        composable(
            route = Destinations.PLAYER_ROUTE,
            arguments = listOf(navArgument(Destinations.PLAYER_ARG_RECORDING_ID) { type = NavType.StringType }),
        ) { backStackEntry ->
            val recordingId = backStackEntry.arguments?.getString(Destinations.PLAYER_ARG_RECORDING_ID)
            if (recordingId != null) {
                PlayerScreen(recordingId = recordingId)
            }
        }

        composable(Destinations.SETTINGS) {
            SettingsScreen()
        }
    }
}
