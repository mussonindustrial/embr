plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven(url = "https://nexus.inductiveautomation.com/repository/public/")
    maven(url = "https://nexus.inductiveautomation.com/repository/inductiveautomation-releases/")
    maven(url = "https://nexus.inductiveautomation.com/repository/inductiveautomation-snapshots/")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.0")
    implementation("com.github.node-gradle:gradle-node-plugin:7.1.0")
    implementation("gradle.plugin.io.ia.sdk:gradle-module-plugin:0.4.0")
    implementation("com.diffplug.spotless:spotless-plugin-gradle:6.25.0")
}