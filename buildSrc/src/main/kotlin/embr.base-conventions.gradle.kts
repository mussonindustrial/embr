import org.jetbrains.kotlin.gradle.targets.js.npm.fromSrcPackageJson

group = "com.mussonindustrial.embr"

plugins {
    base
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven(url = "https://nexus.inductiveautomation.com/repository/public/")
    maven(url = "https://nexus.inductiveautomation.com/repository/inductiveautomation-releases/")
}

val packageJsonFile = file("./package.json")
version = if (packageJsonFile.exists()) {
    val packageJson = fromSrcPackageJson(file("./package.json"))
    packageJson?.version ?: "0.0.0-SNAPSHOT"
} else {
    "0.0.0-SNAPSHOT"
}