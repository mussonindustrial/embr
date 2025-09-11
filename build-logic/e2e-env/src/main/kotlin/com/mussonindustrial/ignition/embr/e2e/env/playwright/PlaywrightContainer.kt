package com.mussonindustrial.ignition.embr.e2e.env.playwright

import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

class PlaywrightContainer(image: String) :
    GenericContainer<PlaywrightContainer>(DockerImageName.parse(image)) {

    init {
        withExposedPorts(3000)
        withCommand(
            "/bin/sh",
            "-c",
            "npx -y playwright@1.55.0 run-server --port 3000 --host 0.0.0.0",
        )
        withWorkingDirectory("/home/pwuser")
        withCreateContainerCmdModifier { cmd -> cmd.withUser("pwuser") }
    }

    val websocketUrl: String
        get() = "ws://${this.host}:${this.getMappedPort(3000)}"
}
