import com.github.gradle.node.npm.task.NpxTask
import com.mussonindustrial.testcontainers.ignition.GatewayEdition
import com.mussonindustrial.testcontainers.ignition.GatewayModule
import io.ia.sdk.gradle.modl.task.Deploy

plugins {
    base
    id("com.github.node-gradle.node")
    id("com.mussonindustrial.gradle.ignition-gateway-plugin")
}

repositories {
    mavenCentral()
    mavenLocal()
    google()
    gradlePluginPortal()
}

ignitionGateway {
    gatewayName.set("embr-development")
    username.set("admin")
    password.set("password")
    edition.set(GatewayEdition.STANDARD)
    modules.set(
        setOf(GatewayModule.PERSPECTIVE)
    )
    thirdPartyModules.set(
        fileTree("build/modules").files
    )
}

tasks.startGateway {
    dependsOn(tasks.build)
}

val deployAll by tasks.registering {
    group = "Ignition"

    val deployModlTasks = subprojects.flatMap { subproject ->
        subproject.tasks.named { it == "deployModl" }
    }

    val lockFile = tasks.startGateway.flatMap { it.lockFile }
    val lockFileContents = lockFile.map { it.get()!! }
    val url = lockFileContents.map { it.url }

    deployModlTasks.forEach { task ->
        (task as Deploy).setGateway(url.get())
    }

    dependsOn(deployModlTasks)
}

val watch by tasks.registering {
    group = "Ignition"
    dependsOn(tasks.startGateway)
    dependsOn(deployAll)
    inputs.dir(".")
}

tasks.build {
    dependsOn(zipModules)
}

tasks.clean {
    dependsOn(tasks.stopGateway)
}

val zipModules by tasks.registering(Zip::class) {
    group = "ignition module"
    inputs.files(assembleModules.get().outputs)

    archiveBaseName.set("modules")
    destinationDirectory.set(file("build"))
    from(assembleModules.get().destinationDir)
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

val release by tasks.registering {
    group = "publishing"
    dependsOn(tasks.build, changesetVersion, changesetPublish)
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
