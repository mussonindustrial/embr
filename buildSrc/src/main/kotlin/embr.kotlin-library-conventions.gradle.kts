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


tasks.jar {
    var parent = project.parent
    val parents = mutableListOf<String>()
    while (parent != null) {
        parents.add(parent.name)
        parent = parent.parent
    }
    parents.reverse()
    val baseName = parents.joinToString("-")
    archiveBaseName.set("${baseName}-${project.name}")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}