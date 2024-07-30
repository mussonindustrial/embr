plugins {
    id("embr.ignition-module-library-conventions")
}

dependencies {
    compileOnly(libs.bundles.common)
    compileOnly(projects.lib.embrCoreCommon)
    compileOnly(projects.modules.embrEventStream.common)

    compileOnly(libs.bundles.gateway)
    modlImplementation(projects.lib.embrCoreGateway)

    compileOnly(libs.bundles.perspectiveGateway)

    compileOnly(libs.jetty.server)
    compileOnly(libs.jetty.servlet)
    modlImplementation(libs.jetty.servlets)
    modlImplementation(projects.lib.embrCoreServlets)
}
