plugins {
    id("embr.library-core-conventions")
}

dependencies {
    compileOnly(libs.bundles.client)
    compileOnly(projects.libraries.core.common)
}
