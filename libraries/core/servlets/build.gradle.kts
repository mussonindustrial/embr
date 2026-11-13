plugins {
    id("embr.build.library-core")
}

dependencies {
    compileOnly(libs.bundles.gateway)
    compileOnly(projects.libraries.core.common)
    compileOnly(projects.libraries.core.gateway)
}
