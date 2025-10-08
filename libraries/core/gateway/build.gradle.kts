plugins {
    id("embr.build.library-core")
}

dependencies {
    compileOnly(libs.bundles.gateway)
    compileOnly(projects.libraries.core.common)
}
