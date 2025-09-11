package com.mussonindustrial.ignition.embr.e2e.env

import com.mussonindustrial.ignition.embr.e2e.env.playwright.PlaywrightContainer
import com.mussonindustrial.testcontainers.ignition.IgnitionContainer
import java.util.stream.Stream
import kotlin.io.path.listDirectoryEntries
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.testcontainers.containers.Network

abstract class TestEnvironmentService : BuildService<TestEnvironmentService.Params>, AutoCloseable {

    interface Params : BuildServiceParameters {
        val ignitionDockerImage: Property<String>
        val ignitionGatewayBackup: RegularFileProperty
        val ignitionModulesDir: DirectoryProperty
        val playwrightDockerImage: Property<String>
    }

    val testNetwork: Network = Network.newNetwork()

    val ignitionContainer: IgnitionContainer by lazy {
        IgnitionContainer(parameters.ignitionDockerImage.get()).run {
            acceptLicense()
            withExposedPorts(8088)
            withGatewayBackup(parameters.ignitionGatewayBackup.get().asFile.toPath())
            withThirdPartyModules(
                *parameters.ignitionModulesDir
                    .get()
                    .asFile
                    .toPath()
                    .listDirectoryEntries("*.modl")
                    .toTypedArray()
            )
            withNetworkAliases("ignition")
            withNetwork(testNetwork)
            withReuse(true)
            this
        }
    }

    val playwrightContainer: PlaywrightContainer by lazy {
        PlaywrightContainer(parameters.playwrightDockerImage.get()).run {
            withNetworkAliases("playwright")
            withNetwork(testNetwork)
            this
        }
    }

    fun start() {
        Stream.of(ignitionContainer, playwrightContainer).parallel().forEach { it.start() }
    }

    override fun close() {
        ignitionContainer.stop()
        playwrightContainer.stop()
    }
}
