plugins {
    id("embr.ignition-module-library-conventions")
}

val sdk = libs.ignition.sdk81
val core = projects.libraries.core.ignition81
val module = projects.modules.sse.ignition81

dependencies {
    compileOnly(sdk.common)
    compileOnly(sdk.gateway)
    compileOnly(sdk.gson)
    compileOnly(sdk.jetty.server)
    compileOnly(sdk.jetty.servlet)
    modlImplementation(sdk.jetty.servlets)
    compileOnly(sdk.perspective.common)
    compileOnly(sdk.perspective.gateway)

    modlImplementation(core.common)
    modlImplementation(core.gateway)
    modlImplementation(core.servlets)

    modlImplementation(module.common)
}