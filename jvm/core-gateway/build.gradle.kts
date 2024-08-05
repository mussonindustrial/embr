plugins {
    id("embr.kotlin-library-conventions")
}

dependencies {
    compileOnly(libs.bundles.common)
    compileOnly(libs.bundles.gateway)

    compileOnly(projects.jvm.coreCommon)
}
