plugins {
    id("embr.ignition-module-library-conventions")
}

group = "com.mussonindustrial.embr.charts"

dependencies {
    compileOnly(libs.bundles.perspectiveCommon)
    modlImplementation(projects.libraries.core.common)
    modlImplementation(projects.libraries.perspective.common)
}