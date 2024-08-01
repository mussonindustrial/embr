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


tasks.register("buildModules") {
    group = "build"
    dependsOn(":modules:embr-charts:build")
    dependsOn(":modules:embr-tag-stream:build")
}

val changesetVersion = tasks.register<NpxTask>("changesetVersion") {
    group = "changesets"
    dependsOn(tasks.build)
    command.set("changeset")
    args.set(listOf("version"))
}

val changesetPublish = tasks.register<NpxTask>("changesetPublish") {
    group = "changesets"
    dependsOn(tasks.build)
    mustRunAfter(changesetVersion)
    command.set("changeset")
    args.set(listOf("publish"))
}

val release = tasks.register("release") {
    group = "publishing"
    dependsOn(changesetVersion, changesetPublish)
}