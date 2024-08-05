rootProject.name = "embr"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

include(
    ":",

    ":jvm:core-common",
    ":jvm:core-servlets",

    ":js:chart-js",
    ":js:event-stream",

    ":modules:charts:common",
    ":modules:charts:designer",
    ":modules:charts:gateway",

    ":modules:event-stream:common",
    ":modules:event-stream:gateway",
)

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")