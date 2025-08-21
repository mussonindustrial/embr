package com.mussonindustrial.ignition.embr.e2e.env

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

open class TestEnvironmentExtension(objects: ObjectFactory) {
    val ignitionImageTag: Property<String> = objects.property(String::class.java)
    val ignitionGatewayBackup: RegularFileProperty = objects.fileProperty()
    val ignitionModulesDir: DirectoryProperty = objects.directoryProperty()

    val playwrightImageTag: Property<String> = objects.property(String::class.java)
}