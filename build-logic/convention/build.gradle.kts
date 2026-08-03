plugins {
    `kotlin-dsl`
}

group = "com.yourdomain.voicescribe.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}

// Expose the root version catalog (gradle/libs.versions.toml) to this
// included build so convention plugins can reference `libs.xxx` too.

// Register each convention plugin class under the `voicescribe.*` plugin ID
// that every module's `plugins {}` block applies it by. `kotlin-dsl` does
// NOT do this automatically from class names alone -- without this block,
// `id("voicescribe.android.application")` (etc.) fails at sync time with
// "Plugin with id '...' not found", since nothing binds the ID string to the
// implementation class.
gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "voicescribe.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "voicescribe.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "voicescribe.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("androidFeature") {
            id = "voicescribe.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
    }
}
