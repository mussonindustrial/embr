import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("embr.base-conventions")
    kotlin("jvm")
//    id("pl.allegro.tech.build.axion-release")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}