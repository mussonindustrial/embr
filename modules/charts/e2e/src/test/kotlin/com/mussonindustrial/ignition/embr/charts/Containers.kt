package com.mussonindustrial.ignition.embr.charts

import com.mussonindustrial.testcontainers.ignition.IgnitionContainer
import io.kotest.extensions.testcontainers.TestContainerProjectExtension

object Containers {
    val gateway =
        IgnitionContainer("inductiveautomation/ignition:8.1.33").apply {
            acceptLicense()
            withExposedPorts(8088)
        }
    val extension = TestContainerProjectExtension(gateway)
}
