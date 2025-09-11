plugins {
    id("embr.build.library-core")
}

dependencies {
    compileOnly(libs.bundles.designer)
    compileOnly(projects.libraries.core.common)
}
