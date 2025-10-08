plugins {
    id("embr.build.ignition-module-library")
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
    compileOnly(projects.modules.sse.common)
}
