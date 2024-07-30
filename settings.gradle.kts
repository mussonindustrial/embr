rootProject.name = "embr"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

include(
    ":",

    ":lib:embr-core-common",
    ":lib:embr-core-designer",
    ":lib:embr-core-gateway",
    ":lib:embr-core-servlets",
    ":lib:embr-perspective-common",
    ":lib:embr-perspective-designer",

    ":js:packages:example-perspective-component",
    ":js:packages:embr-chart-js",
    ":js:packages:embr-tag-stream",

    ":modules:embr-charts:common",
    ":modules:embr-charts:designer",
    ":modules:embr-charts:gateway",

    ":modules:embr-event-stream:common",
    ":modules:embr-event-stream:gateway",

    ":modules:example-perspective-component:common",
    ":modules:example-perspective-component:designer",
    ":modules:example-perspective-component:gateway"
)

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")