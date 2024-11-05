plugins {
    id("embr.ignition-module-library-conventions")
}

group = "com.mussonindustrial.embr.charts"

dependencies {
    compileOnly(libs.bundles.perspectiveDesigner)
    compileOnly(projects.libraries.core.common)
    compileOnly(projects.libraries.perspective.common)
    modlImplementation(projects.libraries.core.designer)
    modlImplementation(projects.libraries.perspective.designer)
    compileOnly(projects.modules.charts.common)
}