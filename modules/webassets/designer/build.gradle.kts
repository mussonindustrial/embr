plugins {
    id("embr.ignition-module-library-conventions")
}

dependencies {
    compileOnly(libs.bundles.designer)
    compileOnly(projects.libraries.core.common)
    modlImplementation(projects.libraries.core.designer)

    compileOnly(projects.modules.webassets.common)
}