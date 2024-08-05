plugins {
    id("embr.kotlin-library-conventions")
}

dependencies {
    compileOnly(libs.jetty.server)
    compileOnly(libs.jetty.servlet)
    compileOnly(libs.bundles.gateway)
    compileOnly(projects.jvm.coreCommon)
}