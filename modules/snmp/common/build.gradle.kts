plugins {
    id("embr.build.ignition-module-library")
}

group = "com.mussonindustrial.embr.snmp"

dependencies {
    compileOnly(libs.bundles.common)
    modlImplementation(projects.libraries.core.common)
    modlImplementation(libs.snmp4j)
}