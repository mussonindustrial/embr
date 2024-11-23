package com.mussonindustrial.gradle.ignition.gateway.container

import java.io.File

class ContainerLockFile(private val file: File) {

    data class Contents(val id: String, val url: String) {
        companion object {
            fun deserialize(string: String) : Contents {
                val parts = string.split(",")
                return Contents(
                    parts[0],
                    parts[1]
                )
            }
        }

        fun serialize(): String {
            return "$id,$url"
        }
    }


    private fun isPresent(): Boolean {
        return file.exists()
    }

    fun isLocked(): Boolean {
        if (!isPresent()) {
            return false
        } else {
            val contents = file.readText()
            return contents != ""
        }
    }

    fun get(): Contents? {
        return if (isLocked()) {
            Contents.deserialize(file.readText())
        } else {
            null
        }
    }

    fun lock(container: Contents) {
        file.writeText(container.serialize())
    }

    fun unlock(): Boolean {
        return file.delete()
    }

    fun isContainerRunning(): Boolean {
        if (!this.isLocked()) {
            return false
        }

        val contents = this.get()!!
        return isContainerRunning(contents.id)
    }

    fun stopContainer(): Boolean {
        if (!this.isContainerRunning()) {
            return false
        }

        val contents = this.get()!!
        val process = ProcessBuilder("docker", "stop", contents.id)
            .inheritIO()
            .start()
        process.waitFor()

        if (process.exitValue() == 0) {
            this.unlock()
            return true
        } else {
            return false
        }
    }

}