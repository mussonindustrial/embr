plugins {
    id("embr.kotlin-library-conventions")
}

dependencies {
    compileOnly(libs.bundles.common)
    compileOnly(libs.bundles.designer)

    compileOnly(projects.jvm.coreCommon)
}
