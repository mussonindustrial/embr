package com.mussonindustrial.gradle.ignition.gateway.task

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

        if (!lockFile.isLocked()) {
            project.logger.info("Gateway is not running.")
            lockFile.unlock()
            return
        }

        if (lockFile.stopContainer()) {
            project.logger.lifecycle("Ignition Gateway instance stopped successfully.")
        } else {
            project.logger.error("Failed to stop gateway.")
        }
    }
}