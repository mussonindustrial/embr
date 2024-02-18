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
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.21")
    implementation("com.github.node-gradle:gradle-node-plugin:7.0.2")
    implementation("gradle.plugin.io.ia.sdk:gradle-module-plugin:0.1.1")
    implementation("com.coditory.gradle:webjar-plugin:1.3.1")

//    implementation(libs.kotlin.gradle.plugin)
//    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}