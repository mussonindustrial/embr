plugins {
    id("embr.build.ignition-module-library")
}

group = "com.mussonindustrial.embr.thermo"

dependencies {
    compileOnly(libs.bundles.common)
    modlImplementation(libs.if97)
    modlImplementation(projects.libraries.core.common)
}

tasks.compileKotlin {
    compilerOptions {
        // Needed for ReflectiveFunctionDocProvider
        javaParameters = true
    }
}