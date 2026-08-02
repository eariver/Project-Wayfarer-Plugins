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
        maven {
            name = "jitpack-vault-api"
            url = uri("https://jitpack.io")
            content {
                includeGroup("com.github.MilkBowl")
            }
        }
        maven {
            name = "enginehub"
            url = uri("https://maven.enginehub.org/repo/")
            content {
                includeGroup("com.sk89q.worldedit")
                includeGroup("com.sk89q.worldguard")
                includeGroup("org.enginehub.lin-bus")
                includeGroup("com.sk89q.worldedit.worldedit-libs")
                includeGroup("com.sk89q.worldguard.worldguard-libs")
            }
        }
        maven {
            name = "multiverse"
            url = uri("https://repo.onarandombox.com/multiverse-releases")
            content {
                includeGroup("org.mvplugins.multiverse.inventories")
                includeGroup("org.mvplugins.multiverse.core")
            }
        }
    }
}

rootProject.name = "Project-Wayfarer-Plugins"

include(
    "libraries:wayfarer-api",
    "libraries:wayfarer-common",
    "libraries:wayfarer-testkit",
    "testkit:headless-paper",
    "testkit:headless-waymark-fixture",
    "testkit:concrete-waymark-probe",
    "integrations:wayfarer-leafgrapple-adapter",
    "plugins:wayfarer-core",
    "plugins:wayfarer-main",
    "plugins:wayfarer-frontier",
)
