plugins {
    id("embr.ignition-module-library-conventions")
}

kotlin {
    jvmToolchain(libs.versions.java.map(String::toInt).get())
}

dependencies {
    compileOnly(libs.bundles.designer)
    compileOnly(libs.bundles.perspectiveDesigner)
    compileOnly(projects.common)
}