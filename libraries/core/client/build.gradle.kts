plugins {
    id("embr.build.library-core")
}

dependencies {
    compileOnly(libs.bundles.client)
    compileOnly(projects.libraries.core.common)
}
