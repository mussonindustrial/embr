package com.mussonindustrial.gradle.ignition.gateway.container

import com.mussonindustrial.testcontainers.ignition.IgnitionContainer

fun isContainerRunning(id: String): Boolean {
    val process = ProcessBuilder("docker", "ps", "--no-trunc", "-qf", "id=${id}")
        .redirectErrorStream(true)
        .start()
    process.waitFor()

    val output = process.inputStream.bufferedReader().readText().trim()
    return output == id
}

fun stopContainer(id: String): Boolean {
    val process = ProcessBuilder("docker", "stop", id)
        .inheritIO()
        .start()
    process.waitFor()

    return process.exitValue() == 0
}

fun IgnitionContainer.getLockFile(): ContainerLockFile.Contents {
    return ContainerLockFile.Contents(this.containerId, this.gatewayUrl)
}