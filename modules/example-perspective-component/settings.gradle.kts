rootProject.name = "example-perspective-component"

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)

    repositories {
        maven(url = "https://nexus.inductiveautomation.com/repository/public")
        mavenCentral()
    }

    versionCatalogs {
        create("libs") {
            from(files("../../gradle/libs.versions.toml"))
        }
    }

}

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven(url = "https://nexus.inductiveautomation.com/repository/public/")
    }
}


include(":common")
include(":gateway")
include(":designer")

include(":web")
project(":web").projectDir = file("../../web")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
