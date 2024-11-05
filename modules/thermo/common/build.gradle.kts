plugins {
    id("embr.ignition-module-library-conventions")
}

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