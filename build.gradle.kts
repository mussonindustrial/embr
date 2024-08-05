import com.github.gradle.node.npm.task.NpxTask

plugins {
    base
    id("com.github.node-gradle.node")
}

repositories {
    mavenCentral()
    mavenLocal()
    google()
    gradlePluginPortal()
}

val releaseFiles: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    releaseFiles(project(":modules:charts", releaseFiles.name))
    releaseFiles(project(":modules:event-stream", releaseFiles.name))
}

val subBuilds = subprojects.map {
    it.tasks.matching { task -> task.name == "build" }
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
    dependsOn(zipModules, changesetVersion, changesetPublish)
}

val assembleModules by tasks.registering(Copy::class) {
    group = "ignition module"
    inputs.files(releaseFiles)

    from(releaseFiles)
    destinationDir = file("build/modules")
}

val zipModules by tasks.registering(Zip::class) {
    group = "ignition module"
    inputs.files(assembleModules.get().outputs)

    archiveBaseName.set("modules")
    destinationDirectory.set(file("build"))
    from(assembleModules.get().destinationDir)
}

tasks.build {
    dependsOn(zipModules)
}