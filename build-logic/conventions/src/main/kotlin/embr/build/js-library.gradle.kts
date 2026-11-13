package embr.build

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.npm.task.NpxTask
import libs

plugins {
    id("embr.build.kotlin-library")
    alias(libs.plugins.gradle.node)
}

node {
    download.set(true)
    version.set(rootProject.the<NodeExtension>().version)
    workDir.set(gradle.gradleUserHomeDir.resolve("nodejs"))
}

tasks.nodeSetup {
    enabled = false
    dependsOn(rootProject.tasks.named("nodeSetup"))
}

abstract class NxService : BuildService<BuildServiceParameters.None>

val nx = gradle.sharedServices.registerIfAbsent("nx", NxService::class) { maxParallelUsages.set(1) }

val nxBuild =
    tasks.register<NpxTask>("nxBuild") {
        group = "nx"
        dependsOn(rootProject.tasks.named("yarnInstall"))
        usesService(nx)
        command.set("nx")
        args.set(listOf("build"))

        // Let Nx do its own caching.
        outputs.upToDateWhen { false }
    }

val nxClean =
    tasks.register<Delete>("nxClean") {
        group = "nx"
        delete = setOf("${projectDir}/dist")
    }

tasks.processResources {
    dependsOn(nxBuild)
    from("dist") { into("static") }
}

tasks.clean { dependsOn(nxClean) }
