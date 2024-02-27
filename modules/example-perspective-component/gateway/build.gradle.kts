plugins {
    id("embr.ignition-module-library-conventions")
}

dependencies {
    compileOnly(libs.bundles.gateway)
    compileOnly(libs.bundles.perspectiveGateway)
    compileOnly(projects.modules.examplePerspectiveComponent.common)
    modlImplementation(projects.js.packages.examplePerspectiveComponent)
}