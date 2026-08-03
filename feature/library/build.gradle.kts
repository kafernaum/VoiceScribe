plugins {
    id("voicescribe.android.feature")
}

android {
    namespace = "com.yourdomain.voicescribe.feature.library"
}

dependencies {
    implementation(libs.compose.material.icons.extended)
    // rememberLauncherForActivityResult (Storage Access Framework export picker)
    implementation(libs.activity.compose)
}
