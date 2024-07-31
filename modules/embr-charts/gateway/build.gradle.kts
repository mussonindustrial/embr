plugins {
    id("embr.ignition-module-library-conventions")
}

dependencies {
    compileOnly(libs.bundles.gateway)
    compileOnly(libs.bundles.perspectiveGateway)
    compileOnly(projects.modules.embrCharts.common)
    modlImplementation(projects.lib.js.embrChartJs)
}