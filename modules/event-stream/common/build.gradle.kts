plugins {
    id("embr.ignition-module-library-conventions")
}

dependencies {
    compileOnly(libs.bundles.common)
    modlImplementation(projects.jvm.coreCommon)
}
