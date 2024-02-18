plugins {
    id("embr.ignition-module-library-conventions")
}

dependencies {
    compileOnly(libs.bundles.designer)
    compileOnly(libs.bundles.perspectiveDesigner)
    compileOnly(projects.modules.examplePerspectiveComponent.common)
}