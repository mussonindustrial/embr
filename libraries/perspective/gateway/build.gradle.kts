plugins {
    id("embr.build.library-perspective")
}

dependencies {
    compileOnly(libs.bundles.gateway)
    compileOnly(libs.bundles.perspectiveGateway)
    compileOnly(projects.libraries.core.common)
    compileOnly(projects.libraries.core.gateway)
    compileOnly(projects.libraries.perspective.common)
}
