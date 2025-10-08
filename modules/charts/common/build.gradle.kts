plugins {
    id("embr.build.ignition-module-library")
}

group = "com.mussonindustrial.embr.charts"

dependencies {
    compileOnly(libs.bundles.perspectiveCommon)
    modlImplementation(projects.libraries.core.common)
    modlImplementation(projects.libraries.perspective.common)
}