import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.text.SimpleDateFormat
import java.util.*

plugins {
    id("embr.base-conventions")
    kotlin("jvm")
    id("pl.allegro.tech.build.axion-release")
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

fun buildTime(): String { return SimpleDateFormat("yyyyMMddHH").format(Date()) }
scmVersion {
    checks {
        uncommittedChanges.set(false)
    }
    tag {
        prefix.set(project.name)
        versionSeparator.set("-")
    }
    useHighestVersion.set(true)
    versionIncrementer("incrementPatch")
}

version = scmVersion.version