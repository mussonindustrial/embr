plugins {
    id("embr.build.ignition-module-library")
}

dependencies {
    compileOnly(libs.bundles.perspectiveDesigner)
    compileOnly(projects.libraries.core.common)
    modlImplementation(projects.libraries.core.designer)
    compileOnly(projects.libraries.perspective.common)
    modlImplementation(projects.libraries.perspective.designer)
    modlImplementation(projects.modules.periscope.designerWeb)

    compileOnly(projects.modules.periscope.common)
}