package com.mussonindustrial.ignition.embr.charts

import com.mussonindustrial.testcontainers.ignition.IgnitionContainer
import io.kotest.extensions.testcontainers.TestContainerProjectExtension
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries

object Containers {
    val gateway =
        IgnitionContainer("inductiveautomation/ignition:8.1.48").apply {
            acceptLicense()
            withExposedPorts(8088)
            withThirdPartyModules(
                *Path("./build/test-resources/modules")
                    .listDirectoryEntries("*.modl")
                    .toTypedArray()
            )
            withGatewayBackup("./build/resources/test/test.gwbk")
        }
    val extension = TestContainerProjectExtension(gateway)
}
