plugins {
    `java-library`
    alias(libs.plugins.kotlin)
}

kotlin {
    jvmToolchain(libs.versions.java.map(String::toInt).get())
}

dependencies {
    compileOnly(libs.bundles.designer)
    compileOnly(libs.bundles.perspective-designer)
    compileOnly(projects.common)
}