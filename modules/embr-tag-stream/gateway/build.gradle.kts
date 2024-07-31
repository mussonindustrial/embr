plugins {
    id("embr.ignition-module-library-conventions")
}

dependencies {
    compileOnly(libs.bundles.gateway)
    compileOnly(libs.jetty.server)
    compileOnly(libs.jetty.servlet)
    modlImplementation(libs.jetty.servlets)

    compileOnly(projects.lib.jvm.embrCoreCommon)
    modlImplementation(projects.lib.jvm.embrCoreServlets)

    compileOnly(projects.modules.embrTagStream.common)
}