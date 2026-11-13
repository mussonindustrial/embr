import com.github.gradle.node.npm.task.NpxTask
import com.github.gradle.node.yarn.task.YarnTask
import groovy.json.JsonSlurper
import org.jetbrains.kotlin.gradle.targets.js.npm.fromSrcPackageJson

plugins {
    base
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.gradle.node)
}

node {
    download.set(true)
    version.set(nodeVersion())
    workDir.set(gradle.gradleUserHomeDir.resolve("nodejs"))
    yarnWorkDir.set(gradle.gradleUserHomeDir.resolve("yarn"))
    yarnVersion.set((json(file("package.json"))["packageManager"] as String).substringAfter("@"))
}

tasks.nodeSetup { onlyIf { !nodeDir.get().asFile.exists() } }

val yarnInstall by tasks.registering(YarnTask::class) {
    group = "nx"
    args.set(listOf("install", "--frozen-lockfile"))
    inputs.files("package.json", "yarn.lock")
    inputs.files(fileTree(projectDir) {
        include("libraries/**/package.json", "modules/**/package.json")
        exclude("**/node_modules/**", "**/build/**", "**/dist/**")
    })
    outputs.file("node_modules/.yarn-integrity")
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

fun json(file: File) = JsonSlurper().parse(file) as Map<*, *>

fun nodeVersion(): String {
    val devEngines = json(file("package.json"))["devEngines"] as Map<*, *>
    return (devEngines["runtime"] as Map<*, *>)["version"] as String
}

val collectSnapshots by tasks.registering(Copy::class) {
    group = "ignition module"

    val releases = json(file("release-plan.json"))["releases"] as List<*>
    val releasedPackages = releases
        .filterIsInstance<Map<*, *>>()
        .filter { it["type"] != "none" }
        .map { it["name"] }
    val signModuleTasks = subprojects
        .filter { it.file("package.json").exists() }
        .filter { fromSrcPackageJson(it.file("package.json"))?.name in releasedPackages }
        .flatMap { subproject -> subproject.tasks.named { it == "signModule" } }

    from(signModuleTasks)
    into("build/snapshots")
}

tasks.build {
    dependsOn(zipModules)
}
