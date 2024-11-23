rootProject.name = "embr"

pluginManagement {
    includeBuild("plugins")
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
}

include(
    ":",

    ":libraries:core:client",
    ":libraries:core:common",
    ":libraries:core:designer",
    ":libraries:core:gateway",
    ":libraries:core:servlets",

    ":libraries:perspective:common",
    ":libraries:perspective:designer",
    ":libraries:perspective:gateway",

    ":libraries:javascript:event-stream",

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