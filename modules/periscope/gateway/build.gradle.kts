plugins {
    id("embr.ignition-module-library-conventions")
}

dependencies {
    compileOnly(libs.bundles.gateway)
    compileOnly(libs.bundles.perspectiveGateway)
    compileOnly(projects.modules.periscope.common)
    modlImplementation(projects.modules.periscope.web)

    compileOnly(projects.jvm.coreCommon)
    modlImplementation(projects.jvm.coreGateway)
}