plugins {
    id("embr.library-perspective-conventions")
}

dependencies {
    compileOnly(libs.bundles.designer)
    compileOnly(libs.bundles.perspectiveDesigner)
    compileOnly(projects.libraries.perspective.common)
}
