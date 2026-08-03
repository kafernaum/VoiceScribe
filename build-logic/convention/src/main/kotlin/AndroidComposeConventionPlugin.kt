import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

/**
 * `voicescribe.android.compose` — enables Jetpack Compose and applies the
 * Kotlin 2.0 Compose compiler Gradle plugin (the compiler moved out of AGP
 * and into `org.jetbrains.kotlin.plugin.compose` as of Kotlin 2.0).
 */
class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            val extension = extensions.getByType<CommonExtension<*, *, *, *, *, *>>()
            extension.buildFeatures.compose = true

            dependencies {
                val bom = platform(libs.findLibrary("compose-bom").get())
                add("implementation", bom)
                add("androidTestImplementation", bom)
                add("implementation", libs.findLibrary("compose-ui").get())
                add("implementation", libs.findLibrary("compose-ui-graphics").get())
                add("implementation", libs.findLibrary("compose-ui-tooling-preview").get())
                add("implementation", libs.findLibrary("compose-material3").get())
                add("debugImplementation", libs.findLibrary("compose-ui-tooling").get())
                add("debugImplementation", libs.findLibrary("compose-ui-test-manifest").get())
                add("androidTestImplementation", libs.findLibrary("compose-ui-test-junit4").get())
            }
        }
    }
}
