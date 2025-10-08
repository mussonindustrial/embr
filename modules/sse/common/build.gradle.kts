plugins {
    id("embr.build.ignition-module-library")
}

dependencies {
    compileOnly(libs.bundles.common)
    modlImplementation(projects.libraries.core.common)
}
