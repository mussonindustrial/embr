rootProject.name = "embr"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

include(
    ":",

    ":jvm:core-client",
    ":jvm:core-common",
    ":jvm:core-designer",
    ":jvm:core-gateway",
    ":jvm:core-servlets",
    ":jvm:perspective-common",
    ":jvm:perspective-designer",

    ":js:chart-js",
    ":js:event-stream",

    ":modules:charts:common",
    ":modules:charts:designer",
    ":modules:charts:gateway",

    ":modules:event-stream:common",
    ":modules:event-stream:gateway",

    ":modules:thermo:common",
    ":modules:thermo:client",
    ":modules:thermo:designer",
    ":modules:thermo:gateway",
)

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")