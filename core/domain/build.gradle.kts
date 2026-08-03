plugins {
    alias(libs.plugins.kotlin.jvm)
}

// core:domain is pure Kotlin/JVM on purpose: use cases, models and repository
// ports have zero Android framework dependency, which makes them runnable
// under plain JUnit5 (no Robolectric/instrumented tests needed) and keeps the
// architecture honest — see docs/adrs/0005-domain-is-pure-kotlin.md.

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.kotlinx.coroutines.core)
    // koin-core is pure Kotlin/JVM (no Android dependency), so use case DI
    // wiring can live next to the use cases themselves — see di/UseCaseModule.kt.
    implementation(libs.koin.core)

    testImplementation(libs.junit5.api)
    testRuntimeOnly(libs.junit5.engine)
    testImplementation(libs.junit5.params)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}

tasks.test {
    useJUnitPlatform()
}
