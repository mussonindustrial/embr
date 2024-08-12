plugins {
    id("embr.kotlin-library-conventions")
}

dependencies {
    compileOnly(libs.bundles.common)
    compileOnly(libs.bundles.client)

    compileOnly(projects.jvm.coreCommon)
}
