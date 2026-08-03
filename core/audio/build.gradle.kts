plugins {
    id("voicescribe.android.library")
}

android {
    namespace = "com.yourdomain.voicescribe.core.audio"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))

    implementation(libs.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.lifecycle.service)
    implementation(libs.koin.android)

    // Silero VAD (ONNX Runtime). Uncomment once assets/models/silero_vad.onnx
    // is bundled — see SileroVadProcessor's KDoc and STT_ENGINES.md.
    // implementation("com.microsoft.onnxruntime:onnxruntime-android:1.19.2")

    testImplementation(libs.junit5.api)
    testRuntimeOnly(libs.junit5.engine)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk.android)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlinx.coroutines.test)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
