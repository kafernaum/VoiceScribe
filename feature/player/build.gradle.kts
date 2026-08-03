plugins {
    id("voicescribe.android.feature")
}

android {
    namespace = "com.yourdomain.voicescribe.feature.player"
}

dependencies {
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.session)
    implementation(libs.media3.common)
    implementation(libs.compose.material.icons.extended)
}
