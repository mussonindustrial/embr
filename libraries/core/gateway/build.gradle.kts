plugins {
    id("embr.library-core-conventions")
}

dependencies {
    compileOnly(libs.bundles.gateway)
    compileOnly(projects.libraries.core.common)
}
