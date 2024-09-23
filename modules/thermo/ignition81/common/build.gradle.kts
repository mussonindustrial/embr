plugins {
    id("embr.ignition-module-library-conventions")
}

val sdk = libs.ignition.sdk81
val core = projects.core.ignition81
val module = projects.thermo.ignition81

dependencies {
    compileOnly(sdk.common)
    compileOnly(sdk.client)
    compileOnly(sdk.gson)

    modlImplementation(core.common)
    modlImplementation(libs.if97)
}

tasks.compileKotlin {
    compilerOptions {
        // Needed for ReflectiveFunctionDocProvider
        javaParameters = true
    }
}