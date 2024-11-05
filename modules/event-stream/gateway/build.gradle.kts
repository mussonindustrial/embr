plugins {
    id("embr.ignition-module-library-conventions")
}

dependencies {
    compileOnly(libs.bundles.gateway)
    compileOnly(libs.bundles.perspectiveGateway)
    compileOnly(libs.jetty.server)
    compileOnly(libs.jetty.servlet)
    modlImplementation(libs.jetty.servlets)
    compileOnly(projects.libraries.core.common)
    modlImplementation(projects.libraries.core.gateway)
    modlImplementation(projects.libraries.core.servlets)
    compileOnly(projects.modules.eventStream.common)
}
