package com.mussonindustrial.gradle.ignition.gateway.container

import com.mussonindustrial.testcontainers.ignition.IgnitionContainer

fun IgnitionContainer.getLockFile(): ContainerLockFile.Contents {
    return ContainerLockFile.Contents(this.containerId, this.gatewayUrl)
}