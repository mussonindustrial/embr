plugins {
    id("embr.build.ignition-module-library")
}

dependencies {
    compileOnly(libs.bundles.designer)
    compileOnly(libs.if97)
    compileOnly(projects.libraries.core.common)
    modlImplementation(projects.libraries.core.designer)
    compileOnly(projects.modules.thermo.common)
    compileOnly(projects.modules.thermo.client)

}
