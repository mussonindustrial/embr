package com.mussonindustrial.ignition.embr.charts

import com.mussonindustrial.testcontainers.ignition.IgnitionContainer
import io.kotest.extensions.testcontainers.TestContainerProjectExtension

object Containers {
    val gateway =
        IgnitionContainer("inductiveautomation/ignition:8.1.48").apply {
            acceptLicense()
            withExposedPorts(8088)
            withThirdPartyModules("./build/test-resources/module.modl")
            withGatewayBackup("./build/resources/test/test.gwbk")
        }
    val extension = TestContainerProjectExtension(gateway)
}
