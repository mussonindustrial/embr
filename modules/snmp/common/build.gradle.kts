plugins {
    id("embr.ignition-module-library-conventions")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    compileOnly(libs.bundles.common)
    modlImplementation(projects.libraries.core.common)
}