plugins {
    alias(libs.plugins.kotlin.jvm)
}

// core:common is deliberately pure Kotlin/JVM — no Android dependency at all.
// It holds cross-cutting utilities that both the domain layer and every
// Android module can use without pulling in the Android framework.
// See docs/adrs/0005-domain-is-pure-kotlin.md.

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit5.api)
    testRuntimeOnly(libs.junit5.engine)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
}

tasks.test {
    useJUnitPlatform()
}
