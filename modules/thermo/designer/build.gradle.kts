plugins {
    id("embr.ignition-module-library-conventions")
}

dependencies {
    compileOnly(libs.bundles.common)
    compileOnly(projects.jvm.coreCommon)

    compileOnly(libs.bundles.designer)
    modlImplementation(projects.jvm.coreDesigner)

    compileOnly(projects.modules.thermo.common)
    compileOnly(projects.modules.thermo.client)
    compileOnly(libs.if97)
}
