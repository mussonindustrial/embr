plugins {
    id("embr.ignition-module-library-conventions")
}

val sdk = libs.ignition.sdk81
val core = projects.core.ignition81
val module = projects.modules.charts.ignition81

dependencies {
    compileOnly(sdk.common)
    compileOnly(sdk.perspective.common)
    compileOnly(sdk.gson)

    modlImplementation(core.common)
}