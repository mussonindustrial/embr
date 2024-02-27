plugins {
    id("embr.ignition-module-library-conventions")
}

dependencies {
    compileOnly(libs.bundles.gateway)
    compileOnly(libs.bundles.perspectiveGateway)
    compileOnly(projects.modules.embrChartJs.common)
    modlImplementation(projects.js.packages.embrChartJs)
}