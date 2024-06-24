plugins {
    id("embr.ignition-module-library-conventions")
}

dependencies {
    compileOnly(projects.lib.embrCoreCommon)
    compileOnly(projects.modules.embrCharts.common)

    compileOnly(libs.bundles.gateway)
    compileOnly(libs.bundles.perspectiveGateway)
    modlImplementation(projects.lib.embrCoreGateway)

    modlImplementation(projects.js.packages.embrChartJs)
}