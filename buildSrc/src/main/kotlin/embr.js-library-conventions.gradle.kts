import com.github.gradle.node.npm.task.NpxTask
import gradle.kotlin.dsl.accessors._6964a05bebf46e68d5f8b1e476ef19a8.processResources

plugins {
    id("embr.base-conventions")
    id("com.github.node-gradle.node")
    `java-library`
}

repositories {
    ivy {
        name = "Node.js"
        setUrl("https://nodejs.org/dist/")
        patternLayout {
            artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]")
        }
        metadataSources {
            artifact()
        }
        content {
            includeModule("org.nodejs", "node")
        }
    }
}

node {
    fastNpmInstall.set(true)
    workDir = file("${rootProject.projectDir}/.gradle/nodejs")
    npmWorkDir = file("${rootProject.projectDir}/.gradle/npm")
    yarnWorkDir = file("${rootProject.projectDir}/.gradle/yarn")
    distBaseUrl.set("https://nodejs.org/dist")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.register<NpxTask>("nxBuild") {
    dependsOn(tasks.npmInstall)
    command.set("nx")
    args.set(listOf("build"))

    // Let NX do its own caching.
    outputs.upToDateWhen { false }
}

tasks.create<Delete>("nxClean") {
    group = "node"
    delete = setOf("${projectDir}/dist")
}

tasks.processResources {
    dependsOn("nxBuild")
    from( "dist") { into("static") }
}

tasks.clean {
    dependsOn("nxClean")
}