plugins {
    id("embr.ignition-module-library-conventions")
}

val sdk = libs.ignition.sdk81
val core = projects.core.ignition81
val module = projects.modules.charts.ignition81
val web = projects.modules.charts.core.web

dependencies {
    compileOnly(sdk.common)
    compileOnly(sdk.gateway)
    compileOnly(sdk.perspective.common)
    compileOnly(sdk.perspective.gateway)
    compileOnly(sdk.gson)

    modlImplementation(core.common)
    modlImplementation(core.gateway)

    modlImplementation(module.common)
    modlImplementation(web)
}