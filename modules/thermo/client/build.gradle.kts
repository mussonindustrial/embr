plugins {
    id("embr.ignition-module-library-conventions")
}

dependencies {
    compileOnly(libs.bundles.common)
    compileOnly(projects.jvm.coreCommon)

    compileOnly(libs.bundles.client)
    modlImplementation(projects.jvm.coreClient)

    compileOnly(projects.modules.thermo.common)
    compileOnly(libs.if97)
}
