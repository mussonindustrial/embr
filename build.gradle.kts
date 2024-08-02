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


val subBuilds = subprojects.map {
    it.tasks.matching { task -> task.name == "build" }
}

val changesetVersion = tasks.register<NpxTask>("changesetVersion") {
    group = "changesets"
    command.set("changeset")
    args.set(listOf("version"))
}

val changesetPublish = tasks.register<NpxTask>("changesetPublish") {
    group = "changesets"
    mustRunAfter(zipModules, changesetVersion)
    command.set("changeset")
    args.set(listOf("publish"))
}

val release = tasks.register("release") {
    group = "publishing"
    dependsOn(zipModules, changesetVersion, changesetPublish)
}


val releaseFiles: Configuration = configurations.create("releaseFiles") {
    isCanBeConsumed = false
    isCanBeResolved = true
}
dependencies {
    releaseFiles(project(":modules:embr-charts", releaseFiles.name))
    releaseFiles(project(":modules:embr-tag-stream", releaseFiles.name))
}

val assembleModules = tasks.register<Copy>("assembleModules") {
    group = "ignition module"
    inputs.files(releaseFiles)

    from(releaseFiles)
    destinationDir = file("build/modules")
}

val zipModules = tasks.register<Zip>("zipModules") {
    group = "ignition module"
    inputs.files(assembleModules.get().outputs)

    archiveBaseName.set("modules")
    destinationDirectory.set(file("build"))
    from(assembleModules.get().destinationDir)
}

tasks.build {
    dependsOn(zipModules)
}