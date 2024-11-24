package com.mussonindustrial.gradle.ignition.gateway.task

import com.mussonindustrial.gradle.ignition.gateway.container.Container
import com.mussonindustrial.gradle.ignition.gateway.container.ContainerLockFile
import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

abstract class DeleteGateway @Inject constructor(objects: ObjectFactory): DefaultTask() {

    companion object {
        const val ID = "deleteGateway"
    }

    @get:Input
    val lockFile: Property<ContainerLockFile> = objects.property(ContainerLockFile::class.java)

    @TaskAction
    fun deleteContainer() {
        val lockFile = lockFile.get()
        if (!lockFile.exists()) {
            project.logger.lifecycle("No Ignition container to delete.")
            return
        }

        val contents = lockFile.get()
        if (contents == null) {
            project.logger.lifecycle("Invalid container lockfile found. Reinitializing...")
            lockFile.unlock()
            return
        }

        val container = contents.getContainer()
        if (container.state == Container.State.MISSING) {
            project.logger.lifecycle("Expected container is missing. Reinitializing...")
            lockFile.unlock()
            return
        }

        if (container.remove(true)) {
            project.logger.lifecycle("Ignition container deleted successfully.")
            lockFile.unlock()
        } else {
            project.logger.error("Failed to delete Ignition container.")
        }
    }
}