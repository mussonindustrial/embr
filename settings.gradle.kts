rootProject.name = "embr"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

includeBuild("conventions")
includeBuild("web")
includeBuild("modules/example-perspective-component")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")