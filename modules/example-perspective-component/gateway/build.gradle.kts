plugins {
    id("embr.ignition-module-library-conventions")
}

dependencies {
    compileOnly(libs.bundles.gateway)
    compileOnly(libs.bundles.perspectiveFateway)
    compileOnly(projects.common)
    modlWebResource(project(path = ":web", configuration = "example_perspective_component"))
}