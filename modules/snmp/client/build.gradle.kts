plugins {
    id("embr.build.ignition-module-library")
}

dependencies {
    compileOnly(libs.bundles.client)
    compileOnly(projects.libraries.core.common)
    modlImplementation(projects.libraries.core.client)
    compileOnly(projects.modules.snmp.common)
}