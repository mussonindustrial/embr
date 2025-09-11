plugins {
    id("embr.build.library-perspective")
}

dependencies {
    compileOnly(libs.bundles.designer)
    compileOnly(libs.bundles.perspectiveDesigner)
    compileOnly(projects.libraries.perspective.common)
}
