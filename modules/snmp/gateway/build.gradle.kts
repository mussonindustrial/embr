plugins {
    id("embr.build.ignition-module-library")
}

group = "com.mussonindustrial.embr.snmp"

dependencies {
    compileOnly(libs.bundles.gateway)
    compileOnly(projects.libraries.core.common)
    modlImplementation(projects.libraries.core.gateway)
    compileOnly(projects.modules.snmp.common)
    compileOnly(libs.snmp4j)
}
