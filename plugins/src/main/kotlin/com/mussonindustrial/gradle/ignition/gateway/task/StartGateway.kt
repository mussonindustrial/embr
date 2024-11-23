package com.mussonindustrial.gradle.ignition.gateway.task

import com.mussonindustrial.gradle.ignition.gateway.container.ContainerLockFile
import com.mussonindustrial.gradle.ignition.gateway.container.getLockFile
import com.mussonindustrial.testcontainers.ignition.GatewayEdition
import com.mussonindustrial.testcontainers.ignition.IgnitionContainer
import com.mussonindustrial.testcontainers.ignition.IgnitionModule
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
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

    @get:Optional
    @get:Input
    val gatewayName: Property<String> = objects.property(String::class.java)

    @get:Input
    val username: Property<String> = objects.property(String::class.java)

    @get:Input
    val password: Property<String>  = objects.property(String::class.java)

    @get:Input
    val edition: Property<GatewayEdition>  = objects.property(GatewayEdition::class.java)

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

    @TaskAction
    fun startContainer() {
        try {
            val lockFile = lockFile.get()
            val isLocked = lockFile.isLocked()
            val isRunning = lockFile.isContainerRunning()

            if (isLocked && !isRunning) {
                project.logger.debug("Old lockfile present. Cleaning up.")
                lockFile.unlock()
            }

            if (isLocked && isRunning) {
                project.logger.debug("Ignition Gateway is already running.")
                return
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
                withCredentials(this@StartGateway.username.get(), this@StartGateway.password.get())
                withEdition(edition.get())
                withAdditionalArgs("-Dia.developer.moduleupload=true")

                // Without this, 3rd party modules will not load.
                // It's something filesystem related.
                // Needs more testing.
                withCreateContainerCmdModifier { cmd ->
                    cmd.withUser("0:0")
                }
                acceptLicense()
            }

            if (gatewayName.isPresent) {
                project.logger.debug("Using Gateway Name: {}", gatewayName.get())
                gateway.withGatewayName(gatewayName.get())
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

            gateway.withReuse(true)
            gateway.start()
            lockFile.lock(gateway.getLockFile())

            project.logger.lifecycle("Ignition Gateway started at: ${gateway.gatewayUrl}")

        } catch (e: Throwable) {
            throw RuntimeException("Failed to start the Ignition Gateway container: ${e.message}", e)
        }
    }
}