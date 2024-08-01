rootProject.name = "embr"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

include(
    ":",

    ":lib:jvm:embr-core-common",
    ":lib:jvm:embr-core-servlets",

    ":lib:js:example-perspective-component",
    ":lib:js:embr-chart-js",
    ":lib:js:embr-tag-stream",

    ":modules:embr-charts:common",
    ":modules:embr-charts:designer",
    ":modules:embr-charts:gateway",

    ":modules:embr-tag-stream:common",
    ":modules:embr-tag-stream:gateway",
)

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")