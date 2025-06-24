plugins {
    id("embr.ignition-module-library-conventions")
}

dependencies {
    compileOnly(libs.bundles.gateway)
    compileOnly(projects.libraries.core.common)
    modlImplementation(projects.libraries.core.gateway)
    compileOnly(libs.jetty.server)
    compileOnly(libs.jetty.servlet)
    modlImplementation(libs.jetty.servlets)
    modlImplementation(projects.libraries.core.servlets)
    compileOnly(projects.modules.webassets.common)

    modlImplementation("org.webjars.npm:chart.js:4.5.0")
}