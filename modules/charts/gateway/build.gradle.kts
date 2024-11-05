plugins {
    id("embr.ignition-module-library-conventions")
}

group = "com.mussonindustrial.embr.charts"

dependencies {
    compileOnly(libs.bundles.perspectiveGateway)
    compileOnly(projects.modules.charts.common)
    modlImplementation(projects.modules.charts.web)
}