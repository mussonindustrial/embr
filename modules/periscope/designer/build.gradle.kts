plugins {
    id("embr.ignition-module-library-conventions")
}

dependencies {
    compileOnly(libs.bundles.perspectiveDesigner)
    compileOnly(projects.libraries.core.common)
    modlImplementation(projects.libraries.core.designer)
    modlImplementation(projects.libraries.perspective.designer)

    compileOnly(projects.modules.periscope.common)
}