plugins {
    id("embr.ignition-module-library-conventions")
}

dependencies {
    compileOnly(libs.bundles.common)
    modlImplementation(projects.jvm.coreCommon)
    modlImplementation(libs.if97)
}

tasks.compileKotlin {
    compilerOptions {
        // Needed for ReflectiveFunctionDocProvider
        javaParameters = true
    }
}