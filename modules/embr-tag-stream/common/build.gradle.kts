plugins {
    id("embr.ignition-module-library-conventions")
}

dependencies {
    compileOnly(libs.bundles.common)
    modlImplementation(projects.lib.jvm.embrCoreCommon)
}