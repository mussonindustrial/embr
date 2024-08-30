import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("embr.base-conventions")
    kotlin("jvm")
    id("com.diffplug.spotless")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_17
}

spotless {
    kotlin {
        ktfmt().kotlinlangStyle()
    }
}

tasks.build {
    dependsOn(tasks.spotlessCheck)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}