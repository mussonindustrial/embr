plugins {
    id("embr.ignition-module-library-conventions")
}

kotlin {
    jvmToolchain(libs.versions.java.map(String::toInt).get())
}

dependencies {
    compileOnly(libs.bundles.common)
    compileOnly(libs.bundles.perspectiveCommon)
}