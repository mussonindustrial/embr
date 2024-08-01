import com.github.gradle.node.npm.task.NpxTask
import org.jetbrains.kotlin.gradle.utils.`is`

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


tasks.register("buildModules") {
    group = "build"
    dependsOn(":modules:embr-charts:build")
    dependsOn(":modules:embr-tag-stream:build")
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
    mustRunAfter(subBuilds, changesetVersion)
    command.set("changeset")
    args.set(listOf("publish"))
}

val release = tasks.register("release") {
    group = "publishing"
    subBuilds.forEach {
        dependsOn(it)
    }
    dependsOn(changesetVersion, changesetPublish)
}