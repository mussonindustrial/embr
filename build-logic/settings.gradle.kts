rootProject.name = "build-logic"

plugins {
    id("dev.panuszewski.typesafe-conventions") version "0.10.0"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://nexus.inductiveautomation.com/repository/public/")
        maven(url = "https://nexus.inductiveautomation.com/repository/inductiveautomation-releases/")
        maven(url = "https://nexus.inductiveautomation.com/repository/inductiveautomation-snapshots/")
    }
}

include(
    "conventions",
)