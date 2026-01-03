plugins {
    id("embr.ignition-module-library-conventions")
}

dependencies {
    compileOnly(libs.bundles.common)
    compileOnly(libs.bundles.designer)
    compileOnly(libs.bundles.client)
    compileOnly(projects.modules.snmp.common)
    modlImplementation(projects.libraries.core.common)
    modlImplementation(projects.modules.snmp.common)
    modlImplementation(projects.modules.snmp.client)
}