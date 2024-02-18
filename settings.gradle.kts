rootProject.name = "embr"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

include(
    ":js:packages:example-perspective-component",
    ":js:packages:embr-chart-js",

    ":modules:embr-chart-js:common",
    ":modules:embr-chart-js:designer",
    ":modules:embr-chart-js:gateway",

    ":modules:example-perspective-component:common",
    ":modules:example-perspective-component:designer",
    ":modules:example-perspective-component:gateway"
)

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")