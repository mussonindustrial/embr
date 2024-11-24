package com.mussonindustrial.gradle.ignition.gateway.task

import com.mussonindustrial.gradle.ignition.gateway.container.Container
import com.mussonindustrial.gradle.ignition.gateway.container.ContainerLockFile
import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

abstract class StopGateway @Inject constructor(objects: ObjectFactory): DefaultTask() {

    companion object {
        const val ID = "stopGateway"
    }

    @get:Input
    val lockFile: Property<ContainerLockFile> = objects.property(ContainerLockFile::class.java)

    @TaskAction
    fun stopContainer() {
        val lockFile = lockFile.get()
        if (!lockFile.exists()) {
            project.logger.lifecycle("No Ignition container to stop.")
            return
        }

        val contents = lockFile.get()
        if (contents == null) {
            project.logger.lifecycle("Invalid container lockfile found. Reinitializing...")
            lockFile.unlock()
            return
        }

        val container = contents.getContainer()
        val state = container.state
        if (state == Container.State.MISSING) {
            project.logger.lifecycle("Expected container is missing. Reinitializing...")
            lockFile.unlock()
            return
        }

        if (state == Container.State.STOPPED) {
            project.logger.lifecycle("Ignition container is already stopped.")
            return
        }

        if (container.stop()) {
            project.logger.lifecycle("Ignition container stopped successfully.")
        } else {
            project.logger.error("Failed to stop Ignition container.")
        }
    }
}