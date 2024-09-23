plugins {
    id("embr.ignition-module-library-conventions")
}

val sdk = libs.ignition.sdk81
val core = projects.libraries.core.ignition81
val module = projects.modules.charts.ignition81

dependencies {
    compileOnly(sdk.common)
    compileOnly(sdk.designer)
    compileOnly(sdk.perspective.common)
    compileOnly(sdk.perspective.designer)
    compileOnly(sdk.gson)

    modlImplementation(core.common)
    modlImplementation(core.designer)

    modlImplementation(module.common)
}