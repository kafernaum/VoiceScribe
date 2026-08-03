plugins {
    id("voicescribe.android.library")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.yourdomain.voicescribe.core.data"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))

    implementation(libs.core.ktx)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.room.paging)
    ksp(libs.room.compiler)

    implementation(libs.sqlcipher.android)
    implementation(libs.androidx.sqlite)

    implementation(libs.datastore.preferences)
    implementation(libs.security.crypto)
    implementation(libs.biometric)

    implementation(libs.koin.android)

    testImplementation(libs.junit5.api)
    testRuntimeOnly(libs.junit5.engine)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk.android)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.test.core)

    // Robolectric tests (RecordingRepositoryImplTest) are JUnit4-style;
    // the vintage engine runs them under the same `useJUnitPlatform()` task.
    testImplementation(libs.junit4)
    testImplementation(libs.robolectric)
    testRuntimeOnly(libs.junit.vintage.engine)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
