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
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.21")
    implementation("com.github.node-gradle:gradle-node-plugin:7.1.0")
    implementation("gradle.plugin.io.ia.sdk:gradle-module-plugin:0.4.1")
    implementation("com.diffplug.spotless:spotless-plugin-gradle:7.0.3")
}