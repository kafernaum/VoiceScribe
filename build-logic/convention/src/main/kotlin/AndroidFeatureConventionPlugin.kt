import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * `voicescribe.android.feature` — the common shape of every `feature:*`
 * module: Android library + Compose + Koin + a dependency on `core:domain`
 * and `core:common` (features never depend on `core:data` or `core:audio`
 * directly; they go through domain use cases, per Clean Architecture).
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("voicescribe.android.library")
                apply("voicescribe.android.compose")
            }

            dependencies {
                add("implementation", project(":core:common"))
                add("implementation", project(":core:domain"))

                add("implementation", libs.findLibrary("koin-android").get())
                add("implementation", libs.findLibrary("koin-compose").get())
                add("implementation", libs.findLibrary("lifecycle-viewmodel-compose").get())
                add("implementation", libs.findLibrary("lifecycle-runtime-compose").get())
                add("implementation", libs.findLibrary("compose-navigation").get())

                add("testImplementation", libs.findLibrary("junit5-api").get())
                add("testRuntimeOnly", libs.findLibrary("junit5-engine").get())
                add("testImplementation", libs.findLibrary("turbine").get())
                add("testImplementation", libs.findLibrary("mockk").get())
                add("testImplementation", libs.findLibrary("kotlinx-coroutines-test").get())
            }
        }
    }
}
