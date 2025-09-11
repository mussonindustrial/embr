plugins {
    id("embr.build.library-perspective")
}

dependencies {
    compileOnly(libs.bundles.common)
    compileOnly(libs.bundles.perspectiveCommon)
    compileOnly(projects.libraries.core.common)
}
