plugins {
    id("embr.build.ignition-module-library")
}

dependencies {
    compileOnly(libs.bundles.perspectiveCommon)
    modlImplementation(projects.libraries.core.common)
    modlImplementation(projects.libraries.perspective.common)
}