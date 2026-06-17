plugins {
    id("embr.build.ignition-module-library")
}

group = "com.mussonindustrial.embr.snmp"

dependencies {
    compileOnly(libs.bundles.designer)
    compileOnly(projects.libraries.core.common)
    modlImplementation(projects.libraries.core.designer)
    compileOnly(projects.modules.snmp.common)
    compileOnly(projects.modules.snmp.client)
    compileOnly(libs.snmp4j)
}