plugins {
    id("voicescribe.android.feature")
}

android {
    namespace = "com.yourdomain.voicescribe.feature.recording"
}

dependencies {
    implementation(libs.compose.material.icons.extended)

    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.junit)
}
