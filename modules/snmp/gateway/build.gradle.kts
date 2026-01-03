plugins {
    id("embr.ignition-module-library-conventions")
}

dependencies {
    compileOnly(libs.bundles.gateway)
    compileOnly(projects.libraries.core.common)
    modlImplementation(projects.libraries.core.gateway)
    compileOnly(projects.modules.snmp.common)
    compileOnly(projects.modules.snmp.client)
    modlImplementation(libs.snmp4j)
}
