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
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.23")
    implementation("com.github.node-gradle:gradle-node-plugin:7.0.2")
    implementation("gradle.plugin.io.ia.sdk:gradle-module-plugin:0.3.0")
    implementation("com.coditory.gradle:webjar-plugin:1.3.1")
    implementation("pl.allegro.tech.build:axion-release-plugin:1.17.0")
    implementation("com.github.breadmoirai:github-release:2.4.1")
    implementation("io.github.kotlin-artisans:plugin:0.0.4")
}