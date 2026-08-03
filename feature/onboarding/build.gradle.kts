plugins {
    id("voicescribe.android.feature")
}

android {
    namespace = "com.yourdomain.voicescribe.feature.onboarding"
}

dependencies {
    implementation(libs.activity.compose)
}
