package com.mussonindustrial.gradle.ignition.gateway

import com.mussonindustrial.gradle.ignition.gateway.container.ContainerLockFile
import com.mussonindustrial.gradle.ignition.gateway.extension.GatewaySettings
import com.mussonindustrial.gradle.ignition.gateway.task.StartGateway
import com.mussonindustrial.gradle.ignition.gateway.task.StopGateway
import org.gradle.api.Plugin
import org.gradle.api.Project

class IgnitionGatewayPlugin: Plugin<Project> {

    override fun apply(project: Project) {
        val lockFile = ContainerLockFile(project.file("build/ignition-container.lock"))

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
            this.gatewayName.set(settings.gatewayName)
            this.username.set(settings.username)
            this.password.set(settings.password)
            this.edition.set(settings.edition)
            this.debugMode.set(settings.debugMode)
            this.gatewayBackup.set(settings.gatewayBackup)
            this.modules.set(settings.modules)
            this.thirdPartyModules.set(settings.thirdPartyModules)
        }

        project.tasks.register(StopGateway.ID, StopGateway::class.java) {
            group = "Ignition"
            description = "Stop the running Ignition Gateway container"

            this.lockFile.set(lockFile)
        }
    }
}



