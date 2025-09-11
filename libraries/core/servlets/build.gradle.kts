plugins {
    id("embr.build.library-core")
}

dependencies {
    compileOnly(libs.bundles.gateway)
    compileOnly(libs.jetty.server)
    compileOnly(libs.jetty.servlet)
    compileOnly(projects.libraries.core.common)
    compileOnly(projects.libraries.core.gateway)
}
