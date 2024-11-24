package com.mussonindustrial.gradle.ignition.gateway.container

import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

class ContainerLockFile(private val file: Provider<RegularFile>) {

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

        fun getContainer(): Container {
            return Container(id)
        }
    }

    fun exists(): Boolean {
        return file.get().asFile.exists()
    }

    private fun isLocked(): Boolean {
        if (!this.exists()) {
            return false
        } else {
            val contents =  file.get().asFile.readText()
            return contents != ""
        }
    }

    fun get(): Contents? {
        return if (isLocked()) {
            Contents.deserialize(file.get().asFile.readText())
        } else {
            null
        }
    }

    fun lock(container: Contents) {
        file.get().asFile.writeText(container.serialize())
    }

    fun unlock(): Boolean {
        return  file.get().asFile.delete()
    }

}