plugins {
    base
    kotlin("jvm")
}

repositories {
    mavenCentral()
    mavenLocal()
    gradlePluginPortal()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "11"
    }
}

