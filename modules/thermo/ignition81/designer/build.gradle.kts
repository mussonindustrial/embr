plugins {
    id("embr.ignition-module-library-conventions")
}

val sdk = libs.ignition.sdk81
val core = projects.core.ignition81
val module = projects.modules.thermo.ignition81

dependencies {
    compileOnly(sdk.common)
    compileOnly(sdk.designer)
    compileOnly(sdk.gson)

    modlImplementation(core.common)
    modlImplementation(core.designer)

    modlImplementation(module.common)
    modlImplementation(module.client)
    modlImplementation(libs.if97)
}
