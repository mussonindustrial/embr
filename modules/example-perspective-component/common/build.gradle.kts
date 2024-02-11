plugins {
    `java-library`
    alias(libs.plugins.kotlin)
}

kotlin {
    jvmToolchain(libs.versions.java.map(String::toInt).get())
}

dependencies {
    compileOnly(libs.bundles.common)
    compileOnly(libs.bundles.perpsective-common)
}