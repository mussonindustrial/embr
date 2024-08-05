plugins {
    id("embr.ignition-module-library-conventions")
}

dependencies {
    compileOnly(libs.bundles.gateway)
    compileOnly(libs.jetty.server)
    compileOnly(libs.jetty.servlet)
    modlImplementation(libs.jetty.servlets)

    compileOnly(projects.jvm.coreCommon)
    modlImplementation(projects.jvm.coreServlets)

    compileOnly(projects.modules.eventStream.common)
}