pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven {
            name = "papermc"
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
    }
}

rootProject.name = "Project-Wayfarer-Plugins"

include(
    "libraries:wayfarer-api",
    "libraries:wayfarer-common",
    "libraries:wayfarer-testkit",
    "integrations:wayfarer-leafgrapple-adapter",
    "plugins:wayfarer-core",
    "plugins:wayfarer-main",
    "plugins:wayfarer-frontier",
)
