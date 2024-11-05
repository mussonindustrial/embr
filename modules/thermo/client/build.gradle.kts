plugins {
    id("embr.ignition-module-library-conventions")
}

dependencies {
    compileOnly(libs.bundles.client)
    compileOnly(libs.if97)
    compileOnly(projects.libraries.core.common)
    modlImplementation(projects.libraries.core.client)
    compileOnly(projects.modules.thermo.common)

}
