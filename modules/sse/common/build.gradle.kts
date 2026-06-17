plugins {
    id("embr.build.ignition-module-library")
}

group = "com.mussonindustrial.embr.sse"

dependencies {
    compileOnly(libs.bundles.common)
    modlImplementation(projects.libraries.core.common)
}
