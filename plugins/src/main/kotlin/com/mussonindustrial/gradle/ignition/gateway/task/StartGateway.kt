package com.mussonindustrial.gradle.ignition.gateway.task

import com.mussonindustrial.gradle.ignition.gateway.container.Container
import com.mussonindustrial.gradle.ignition.gateway.container.ContainerLockFile
import com.mussonindustrial.gradle.ignition.gateway.container.getLockFile
import com.mussonindustrial.testcontainers.ignition.GatewayEdition
import com.mussonindustrial.testcontainers.ignition.IgnitionContainer
import com.mussonindustrial.testcontainers.ignition.IgnitionModule
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.*
import org.testcontainers.dockerclient.DockerClientProviderStrategy
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.reflect.KCallable
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.isAccessible


open class StartGateway @Inject constructor(objects: ObjectFactory): DefaultTask() {

    companion object {
        const val ID = "startGateway"
    }

    @get:Input
    val lockFile: Property<ContainerLockFile> = objects.property(ContainerLockFile::class.java)

    @get:Input
    val acceptLicense: Property<Boolean> = objects.property(Boolean::class.java)

    @get:Optional
    @get:Input
    val gatewayName: Property<String> = objects.property(String::class.java)

    @get:Optional
    @get:Input
    val username: Property<String> = objects.property(String::class.java)

    @get:Optional
    @get:Input
    val password: Property<String>  = objects.property(String::class.java)

    @get:Optional
    @get:Input
    val edition: Property<GatewayEdition>  = objects.property(GatewayEdition::class.java)

    @get:Optional
    @get:Input
    val debugMode: Property<Boolean> = objects.property(Boolean::class.java)

    @get:Optional
    @get:Input
    val modules: SetProperty<IgnitionModule> = objects.setProperty(IgnitionModule::class.java)

    @get:Optional
    @get:InputFile
    val gatewayBackup: RegularFileProperty = objects.fileProperty()

    @get:Optional
    @get:Input
    val thirdPartyModules: SetProperty<File> = objects.setProperty(File::class.java)

    @get:Optional
    @get:Input
    val additionalArgs: ListProperty<String> = objects.listProperty(String::class.java)

    @get:Optional
    @get:Input
    val maxMemory: Property<String> = objects.property(String::class.java)

    @TaskAction
    fun startContainer() {
        try {
            val lockFile = lockFile.get()
            if (lockFile.exists()) {
                val contents = lockFile.get()
                if (contents == null) {
                    project.logger.lifecycle("Invalid container lockfile found. Reinitializing...")
                    lockFile.unlock()

                } else {
                    val container = contents.getContainer()
                    val state = container.state
                    when (state) {
                        Container.State.MISSING -> {
                            project.logger.lifecycle("Expected container is missing. Reinitializing...")
                            lockFile.unlock()
                        }

                        Container.State.RUNNING ->  {
                            project.logger.lifecycle("Ignition container is already running: ${lockFile.get()?.url}")
                            return
                        }

                        Container.State.STOPPED -> {
                            if (container.start()) {
                                project.logger.lifecycle("Existing Ignition container resumed: ${lockFile.get()?.url}")
                            } else {
                                project.logger.lifecycle("Failed to resume existing Ignition container")
                            }
                            return
                        }
                    }
                }
            }

            // Work around for https://github.com/testcontainers/testcontainers-java/issues/6441
            val failFastAlways = DockerClientProviderStrategy::class.declaredMembers
                .single { it.name == "FAIL_FAST_ALWAYS" }
                .apply { isAccessible = true }
                .let {
                    @Suppress("UNCHECKED_CAST")
                    it as KCallable<AtomicBoolean>
                }
                .call()
            failFastAlways.set(false)

            val gateway = IgnitionContainer("inductiveautomation/ignition:8.1.33").apply {
                // Without this, 3rd party modules will not load.
                // It's something filesystem related.
                // Needs more testing.
                withCreateContainerCmdModifier { cmd ->
                    cmd.withUser("0:0")
                }
            }

            if (acceptLicense.isPresent) {
                val acceptLicense = acceptLicense.get()
                if (acceptLicense) {
                    project.logger.debug("Using Accept License: {}", acceptLicense)
                    gateway.acceptLicense()
                }
            }

            if (gatewayName.isPresent) {
                val gatewayName = gatewayName.get()
                project.logger.debug("Using Gateway Name: {}", gatewayName)
                gateway.withGatewayName(gatewayName)
            }

            if (username.isPresent && password.isPresent) {
                val username = username.get()
                val password = password.get()
                project.logger.debug("Using credentials: {}:{}", username, password)
                gateway.withCredentials(username, password)
            }

            if (edition.isPresent) {
                val edition = edition.get()
                project.logger.debug("Using Gateway Edition: {}", edition)
                gateway.withEdition(edition)
            }

            if (debugMode.isPresent) {
                val debugMode = debugMode.get()
                project.logger.debug("Using Debug Mode: {}", debugMode)
                gateway.withDebugMode(debugMode)
            }

            if (modules.isPresent) {
                val modules = modules.get()
                project.logger.debug("Using Modules: {}", modules)
                gateway.withModules(*modules.toTypedArray())
            }

            if (gatewayBackup.isPresent) {
                val filePath = gatewayBackup.get().asFile.path
                project.logger.debug("Using Gateway Backup: {}", filePath)
                gateway.withGatewayBackup(filePath)
            }

            if (thirdPartyModules.isPresent) {
                val thirdPartyModules = thirdPartyModules.get()
                project.logger.debug("Using Third Party Modules: {}", thirdPartyModules)
                gateway.withThirdPartyModules(*thirdPartyModules.map { it.path }.toTypedArray())
            }

            if (additionalArgs.isPresent) {
                val additionalArgs = additionalArgs.get()
                project.logger.debug("Using Additional Arguments: {}", additionalArgs)
                gateway.withAdditionalArgs(*additionalArgs.toTypedArray())
            }

            if (maxMemory.isPresent) {
                val maxMemory = maxMemory.get()
                project.logger.debug("Using Max Memory: {}", maxMemory)
                gateway.withMaxMemory(maxMemory)
            }

            gateway.start()

            val lockFileContents = gateway.getLockFile()
            lockFile.lock(lockFileContents)
            project.logger.lifecycle("Gateway started: ${lockFileContents.url}")

        } catch (e: Throwable) {
            throw RuntimeException("Failed to start the Ignition Gateway container: ${e.message}", e)
        }
    }
}