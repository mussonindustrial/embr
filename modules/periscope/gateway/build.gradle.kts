plugins {
    id("embr.ignition-module-library-conventions")
}

dependencies {
    compileOnly(libs.bundles.perspectiveGateway)
    compileOnly(projects.libraries.core.common)
    modlImplementation(projects.libraries.core.gateway)
    compileOnly(projects.libraries.perspective.common)
    modlImplementation(projects.libraries.perspective.gateway)
    compileOnly(projects.modules.periscope.common)
    modlImplementation(projects.modules.periscope.web)
}