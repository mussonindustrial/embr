import com.github.gradle.node.npm.task.NpxTask
import groovy.json.JsonSlurper

plugins {
    base
    alias(libs.plugins.node.gradle)
}

repositories {
    mavenCentral()
    mavenLocal()
    google()
    gradlePluginPortal()
}

val changesetVersion by tasks.registering(NpxTask::class) {
    group = "changesets"
    command.set("changeset")
    args.set(listOf("version"))
}

val changesetPublish by tasks.registering(NpxTask::class) {
    group = "changesets"
    mustRunAfter(zipModules, changesetVersion)
    command.set("changeset")
    args.set(listOf("publish"))
}

val release by tasks.registering {
    group = "publishing"
    dependsOn(tasks.build, changesetVersion, changesetPublish)
}

val assembleModules by tasks.registering(Copy::class) {
    group = "ignition module"

    val signModuleTasks = subprojects.flatMap { subproject ->
        subproject.tasks.named { it == "signModule" }
    }
    val signedModules = signModuleTasks.map {
        it.outputs.files.singleFile
    }


    inputs.files(signedModules)
    dependsOn(signModuleTasks)

    from(signedModules)
    destinationDir = file("build/modules")
}

val zipModules by tasks.registering(Zip::class) {
    group = "ignition module"
    inputs.files(assembleModules.get().outputs)

    archiveBaseName.set("modules")
    destinationDirectory.set(file("build"))
    from(assembleModules.get().destinationDir)
}

fun json(file: File) = JsonSlurper().parse(file) as Map<*, *>

val collectSnapshots by tasks.registering(Copy::class) {
    group = "ignition module"

    val releases = json(file("release-plan.json"))["releases"] as List<*>
    val releasedPackages = releases
        .filterIsInstance<Map<*, *>>()
        .filter { it["type"] != "none" }
        .map { it["name"] }
    val signModuleTasks = subprojects
        .filter { it.file("package.json").exists() }
        .filter { json(it.file("package.json"))["name"] in releasedPackages }
        .flatMap { subproject -> subproject.tasks.named { it == "signModule" } }

    from(signModuleTasks)
    into("build/snapshots")
}

tasks.build {
    dependsOn(zipModules)
}
