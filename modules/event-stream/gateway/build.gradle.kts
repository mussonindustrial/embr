plugins {
    id("embr.ignition-module-library-conventions")
}

dependencies {
    compileOnly(libs.bundles.common)
    compileOnly(projects.jvm.coreCommon)
    compileOnly(projects.modules.eventStream.common)

    compileOnly(libs.bundles.gateway)
    modlImplementation(projects.jvm.coreGateway)

    compileOnly(libs.bundles.perspectiveGateway)

    compileOnly(libs.jetty.server)
    compileOnly(libs.jetty.servlet)
    modlImplementation(libs.jetty.servlets)
    modlImplementation(projects.jvm.coreServlets)

}
