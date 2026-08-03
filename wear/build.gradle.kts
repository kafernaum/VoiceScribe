plugins {
    id("voicescribe.android.application")
    id("voicescribe.android.compose")
}

android {
    namespace = "com.yourdomain.voicescribe.wear"

    defaultConfig {
        applicationId = "com.yourdomain.voicescribe.wear"
        versionCode = 1
        versionName = "1.0.0"
        minSdk = 30 // Wear OS 3+
    }
}

dependencies {
    implementation(libs.wear.compose.material)
    implementation(libs.wear.compose.foundation)
    implementation(libs.play.services.wearable)
    implementation(libs.activity.compose)
    // Icons.Filled.Mic / Icons.Filled.Stop live outside the small
    // material-icons-core set bundled by the compose convention plugin.
    implementation(libs.compose.material.icons.extended)
}
