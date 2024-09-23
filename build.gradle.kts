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

// Depend on the signed modules of all subprojects.
gradle.projectsEvaluated {
    dependencies {
        subprojects.filter {
            it.tasks.matching { task -> task.name == "signModule" }.isNotEmpty()
        }.map {
            releaseFiles(project(it.path, releaseFiles.name))
        }
    }
}

val changesetVersion by tasks.registering(NpxTask::class) {
    group = "changesets"
    description = "Consume all changesets and update to the most appropriate semver version based on the those changesets."
    command.set("changeset")
    args.set(listOf("version"))
}

val changesetPublish by tasks.registering(NpxTask::class) {
    group = "changesets"
    description = "Run npm publish in each package that is of a later version than the one currently listed on npm."
    mustRunAfter(zipModules, changesetVersion)
    command.set("changeset")
    args.set(listOf("publish"))
}

val release by tasks.registering {
    group = "publishing"
    description = "Zip, version, and publish."
    dependsOn(zipModules, changesetVersion, changesetPublish)
}

val assembleModules by tasks.registering(Copy::class) {
    group = "ignition module"
    description = "Collect all modules."
    inputs.files(releaseFiles)

    // Unsure why this is needed. If this line is removed the dependency graph is not correct.
    dependsOn(subprojects.map { it.tasks.matching { task -> task.name == "signModule"} })

    from(releaseFiles)
    destinationDir = file("build/modules")
}

val zipModules by tasks.registering(Zip::class) {
    group = "ignition module"
    description = "Create a zip of all modules."
    inputs.files(assembleModules.get().outputs)

    archiveBaseName.set("modules")
    destinationDirectory.set(file("build"))
    from(assembleModules.get().destinationDir)
}

tasks.build {
    dependsOn(zipModules)
}