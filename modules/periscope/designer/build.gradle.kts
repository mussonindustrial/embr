plugins {
    id("embr.ignition-module-library-conventions")
}

dependencies {
    compileOnly(libs.bundles.designer)
    compileOnly(libs.bundles.perspectiveDesigner)

    modlImplementation(projects.jvm.perspectiveDesigner)
    compileOnly(projects.modules.periscope.common)
}