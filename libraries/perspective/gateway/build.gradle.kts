plugins {
    id("embr.library-perspective-conventions")
}

dependencies {
    compileOnly(libs.bundles.gateway)
    compileOnly(libs.bundles.perspectiveGateway)
    compileOnly(projects.libraries.core.common)
    compileOnly(projects.libraries.core.gateway)
    compileOnly(projects.libraries.perspective.common)
}
