package com.mussonindustrial.gradle.ignition.gateway.container

class Container(private val id: String) {

    enum class State(val text: String) {
        MISSING("missing"),
        STOPPED("stopped"),
        RUNNING("running")
    }

    val state: State
        get() {
            if (!this.exists()) {
                return State.MISSING
            }

            return when (this.isRunning()) {
                true -> State.RUNNING
                false -> State.STOPPED
            }
        }

    private fun isRunning(): Boolean {
        val process = ProcessBuilder("docker", "ps", "--no-trunc", "-qf", "id=${id}")
            .redirectErrorStream(true)
            .start()
        process.waitFor()

        val output = process.inputStream.bufferedReader().readText().trim()
        return output == id
    }

    private fun exists(): Boolean {
        val process = ProcessBuilder("docker", "ps", "--no-trunc", "-aqf", "id=${id}")
            .redirectErrorStream(true)
            .start()
        process.waitFor()

        val output = process.inputStream.bufferedReader().readText().trim()
        return output == id
    }

    fun start(): Boolean {
        val process = ProcessBuilder("docker", "start", id)
            .inheritIO()
            .start()
        process.waitFor()

        return process.exitValue() == 0
    }

    fun stop(): Boolean {
        val process = ProcessBuilder("docker", "stop", id)
            .inheritIO()
            .start()
        process.waitFor()

        return process.exitValue() == 0
    }

    fun remove(): Boolean {
        return remove(false)
    }

    fun remove(force: Boolean): Boolean {
        val forceCommand = if (force) { "--force" } else { "" }
        val process = ProcessBuilder("docker", "rm", forceCommand, id)
            .inheritIO()
            .start()
        process.waitFor()

        return process.exitValue() == 0
    }
}