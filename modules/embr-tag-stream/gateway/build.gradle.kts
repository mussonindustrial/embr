plugins {
    id("embr.ignition-module-library-conventions")
}

dependencies {
    compileOnly(libs.bundles.gateway)
    compileOnly(libs.jetty.server)
    compileOnly(libs.jetty.servlet)
    modlImplementation(libs.jetty.servlets)

    compileOnly(projects.lib.embrCoreCommon)
    modlImplementation(projects.lib.embrCoreServlets)

    compileOnly(projects.modules.embrTagStream.common)
}