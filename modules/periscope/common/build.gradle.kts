plugins {
    id("embr.ignition-module-library-conventions")
}

dependencies {
    compileOnly(libs.bundles.common)
    compileOnly(libs.bundles.perspectiveCommon)

    modlImplementation(projects.jvm.perspectiveCommon)
    modlImplementation(projects.jvm.coreCommon)
}