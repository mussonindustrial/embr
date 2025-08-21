import com.github.gradle.node.npm.task.NpxTask
import com.mussonindustrial.ignition.embr.e2e.env.TestEnvironmentTask

plugins {
    base
    id("com.github.node-gradle.node")
    id("embr.e2e-test-environment")
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
    dependsOn(tasks.build, changesetVersion)
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

tasks.assemble {
    dependsOn(zipModules)
}

tasks.withType<TestEnvironmentTask>().configureEach {
    dependsOn(tasks.assemble)
}

testEnvironment {
    ignitionImageTag = "inductiveautomation/ignition:8.1.48"
    ignitionGatewayBackup = project.layout.projectDirectory.file("e2e.gwbk")
    ignitionModulesDir = file("build/modules")
    playwrightImageTag = "mcr.microsoft.com/playwright:v1.55.0-noble"
}