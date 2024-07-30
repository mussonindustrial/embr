import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.text.SimpleDateFormat
import java.util.*

plugins {
    id("embr.base-conventions")
    kotlin("jvm")
    id("pl.allegro.tech.build.axion-release")
    id("org.jlleitschuh.gradle.ktlint")
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

ktlint {
    additionalEditorconfig.set(
        mapOf(
            "max_line_length" to "off"
        ),
    )
}

tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.KtLintCheckTask>().configureEach {
    dependsOn(tasks.ktlintFormat)
}
