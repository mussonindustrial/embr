plugins {
    id("embr.ignition-module-library-conventions")
}

dependencies {
    compileOnly(libs.bundles.common)
    compileOnly(libs.bundles.client)
    compileOnly(libs.bundles.gateway)
    compileOnly(libs.bundles.designer)
    modlImplementation(projects.libraries.core.common)
    modlImplementation(projects.modules.snmp.common)
}