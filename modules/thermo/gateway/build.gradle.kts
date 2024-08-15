plugins {
    id("embr.ignition-module-library-conventions")
}

dependencies {
    compileOnly(libs.bundles.common)
    compileOnly(projects.jvm.coreCommon)
    compileOnly(projects.modules.thermo.common)

    compileOnly(libs.bundles.gateway)
    modlImplementation(projects.jvm.coreGateway)

    compileOnly(libs.if97)
}
