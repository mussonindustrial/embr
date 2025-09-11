rootProject.name = "embr"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://nexus.inductiveautomation.com/repository/public/")
        maven(url = "https://nexus.inductiveautomation.com/repository/inductiveautomation-releases/")
        maven(url = "https://nexus.inductiveautomation.com/repository/inductiveautomation-snapshots/")
    }
}

includeBuild("build-logic")

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

    ":libraries:testing:e2e-testing",

    ":modules:charts:common",
    ":modules:charts:designer",
    ":modules:charts:gateway",
    ":modules:charts:web",
    ":modules:charts:e2e",

    ":modules:sse:common",
    ":modules:sse:gateway",

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