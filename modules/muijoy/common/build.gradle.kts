plugins {
    id("embr.ignition-module-library-conventions")
}

dependencies {
    compileOnly(libs.bundles.perspectiveCommon)
    modlImplementation(projects.libraries.core.common)
    modlImplementation(projects.libraries.perspective.common)
}