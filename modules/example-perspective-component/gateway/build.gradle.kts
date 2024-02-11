plugins {
    id("embr.ignition-module-library-conventions")
}

kotlin {
    jvmToolchain(libs.versions.java.map(String::toInt).get())
}

dependencies {
    compileOnly(libs.bundles.gateway)
    compileOnly(libs.bundles.perspectiveFateway)
    compileOnly(projects.common)
}