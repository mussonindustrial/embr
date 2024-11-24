package com.mussonindustrial.gradle.ignition.gateway

import com.mussonindustrial.gradle.ignition.gateway.container.ContainerLockFile
import com.mussonindustrial.gradle.ignition.gateway.extension.GatewaySettings
import com.mussonindustrial.gradle.ignition.gateway.extension.applySettings
import com.mussonindustrial.gradle.ignition.gateway.task.DeleteGateway
import com.mussonindustrial.gradle.ignition.gateway.task.StartGateway
import com.mussonindustrial.gradle.ignition.gateway.task.StopGateway
import org.gradle.api.Plugin
import org.gradle.api.Project

class IgnitionGatewayPlugin: Plugin<Project> {

    private fun getContainerLockFile(project: Project): ContainerLockFile {
        return ContainerLockFile(
            project.provider {
                project.layout.projectDirectory.file(".ignition-container.lock")
            }
        )
    }

    override fun apply(project: Project) {
        val lockFile = getContainerLockFile(project)

        val settings = project.extensions.create(
            GatewaySettings.EXTENSION_NAME,
            GatewaySettings::class.java
        )

        setupTasks(project, settings, lockFile)
    }

    private fun setupTasks(project: Project, settings: GatewaySettings, lockFile: ContainerLockFile) {
        project.tasks.register(StartGateway.ID, StartGateway::class.java) {
            group = "Ignition"
            description = "Start an Ignition Gateway container"

            this.lockFile.set(lockFile)
            this.applySettings(settings)
        }

        project.tasks.register(StopGateway.ID, StopGateway::class.java) {
            group = "Ignition"
            description = "Stop the running Ignition Gateway container"

            this.lockFile.set(lockFile)
        }

        project.tasks.register(DeleteGateway.ID, DeleteGateway::class.java) {
            group = "Ignition"
            description = "Delete the Ignition Gateway container"

            this.lockFile.set(lockFile)
        }
    }
}



