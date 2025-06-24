plugins {
    id("embr.ignition-module-library-conventions")
}

group = "com.mussonindustrial.embr.charts"

dependencies {
    compileOnly(libs.bundles.perspectiveGateway)
    compileOnly(projects.libraries.core.common)
    modlImplementation(projects.libraries.core.gateway)
    compileOnly(projects.libraries.perspective.common)
    modlImplementation(projects.libraries.perspective.gateway)
    compileOnly(projects.modules.charts.common)
    modlImplementation(projects.modules.charts.web)

    modlImplementation("org.webjars.npm:chart.js:4.4.9")
}