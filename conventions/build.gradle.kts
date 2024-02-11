plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven(url = "https://nexus.inductiveautomation.com/repository/public/")
    maven(url = "https://nexus.inductiveautomation.com/repository/inductiveautomation-thirdparty/")
    maven(url = "https://nexus.inductiveautomation.com/repository/inductiveautomation-releases/")
    maven(url = "https://nexus.inductiveautomation.com/repository/inductiveautomation-snapshots/")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.21")
    implementation("gradle.plugin.io.ia.sdk:gradle-module-plugin:0.1.1")
}