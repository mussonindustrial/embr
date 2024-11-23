package com.mussonindustrial.gradle.ignition.gateway.container

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

    if (process.exitValue() == 0) {
        return true
    } else {
        return false
    }
}