plugins {
    id("embr.ignition-module-library-conventions")
}

val sdk = libs.ignition.sdk81
val core = projects.core.ignition81
val module = projects.modules.thermo.ignition81

dependencies {
    compileOnly(sdk.common)
    compileOnly(sdk.client)
    compileOnly(sdk.vision.client)
    compileOnly(sdk.gson)

    modlImplementation(core.common)
    modlImplementation(core.client)

    modlImplementation(module.common)
    modlImplementation(libs.if97)
}
