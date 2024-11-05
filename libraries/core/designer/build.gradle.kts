plugins {
    id("embr.library-core-conventions")
}

dependencies {
    compileOnly(libs.bundles.designer)
    compileOnly(projects.libraries.core.common)
}
