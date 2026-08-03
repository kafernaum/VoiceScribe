package com.yourdomain.voicescribe.app.navigation

object Destinations {
    const val ONBOARDING = "onboarding"
    const val RECORDING = "recording"
    const val LIBRARY = "library"
    const val PLAYER_ROUTE = "player/{recordingId}"
    const val SETTINGS = "settings"

    const val PLAYER_ARG_RECORDING_ID = "recordingId"

    fun player(recordingId: String): String = "player/$recordingId"
}
