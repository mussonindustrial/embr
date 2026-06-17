plugins {
    id("embr.build.ignition-module-library")
}

group = "com.mussonindustrial.embr.thermo"

dependencies {
    compileOnly(libs.bundles.gateway)
    compileOnly(libs.if97)
    compileOnly(projects.libraries.core.common)
    modlImplementation(projects.libraries.core.gateway)
    compileOnly(projects.modules.thermo.common)
}
