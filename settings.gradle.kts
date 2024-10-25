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

    ":js:event-stream",

    ":modules:charts:common",
    ":modules:charts:designer",
    ":modules:charts:gateway",
    ":modules:charts:web",

    ":modules:event-stream:common",
    ":modules:event-stream:gateway",

    ":modules:periscope:common",
    ":modules:periscope:designer",
    ":modules:periscope:gateway",
    ":modules:periscope:web",

    ":modules:thermo:common",
    ":modules:thermo:client",
    ":modules:thermo:designer",
    ":modules:thermo:gateway",
)

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")