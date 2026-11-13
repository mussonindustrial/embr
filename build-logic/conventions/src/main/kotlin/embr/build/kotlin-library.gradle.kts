package embr.build

import libs
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    id("embr.build.base")
    alias(libs.plugins.kotlin)
    alias(libs.plugins.spotless)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin { jvmToolchain(libs.versions.java.get().toInt()) }

spotless { kotlin { ktfmt().kotlinlangStyle() } }

tasks.build { dependsOn(tasks.spotlessCheck) }

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        apiVersion.set(KotlinVersion.KOTLIN_1_9)
    }
}
