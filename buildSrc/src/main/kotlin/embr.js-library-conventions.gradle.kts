import com.github.gradle.node.npm.task.NpxTask

plugins {
    id("embr.kotlin-library-conventions")
    id("com.github.node-gradle.node")
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
    workDir = file("${rootProject.projectDir}/.gradle/nodejs")
    npmWorkDir = file("${rootProject.projectDir}/.gradle/npm")
    yarnWorkDir = file("${rootProject.projectDir}/.gradle/yarn")
    distBaseUrl.set("https://nodejs.org/dist")
}

tasks.nodeSetup {
    enabled = false
}
tasks.npmInstall {
    enabled = false
}

val nxBuild = tasks.register<NpxTask>("nxBuild") {
    group = "nx"
    dependsOn(tasks.npmInstall)
    command.set("nx")
    args.set(listOf("build"))

    // Let Nx do its own caching.
    outputs.upToDateWhen { false }
}

val nxClean = tasks.create<Delete>("nxClean") {
    group = "nx"
    delete = setOf("${projectDir}/dist")
}

tasks.processResources {
    dependsOn(nxBuild)
    from( "dist") { into("static") }
}

tasks.clean {
    dependsOn(nxClean)
}