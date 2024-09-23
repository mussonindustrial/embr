plugins {
    id("embr.ignition-module-library-conventions")
}

val sdk = libs.ignition.sdk81
val core = projects.libraries.core.ignition81
val module = projects.modules.sse.ignition81

dependencies {
    compileOnly(sdk.common)
    compileOnly(sdk.gson)

    modlImplementation(core.common)
}