rootProject.name = "embr"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

include(
    ":",

    ":js:packages:example-perspective-component",
    ":js:packages:embr-chart-js",

    ":modules:embr-charts:common",
    ":modules:embr-charts:designer",
    ":modules:embr-charts:gateway",

    ":modules:embr-tag-stream:common",
    ":modules:embr-tag-stream:gateway",

    ":modules:example-perspective-component:common",
    ":modules:example-perspective-component:designer",
    ":modules:example-perspective-component:gateway"
)

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")