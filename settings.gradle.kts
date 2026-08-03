pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "VoiceScribe"

include(
    ":app",
    ":core:common",
    ":core:domain",
    ":core:data",
    ":core:audio",
    ":feature:recording",
    ":feature:library",
    ":feature:player",
    ":feature:settings",
    ":feature:onboarding",
    ":wear",
)
