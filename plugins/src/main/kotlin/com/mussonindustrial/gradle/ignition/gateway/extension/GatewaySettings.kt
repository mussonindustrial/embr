package com.mussonindustrial.gradle.ignition.gateway.extension

import com.mussonindustrial.gradle.ignition.gateway.task.StartGateway
import com.mussonindustrial.testcontainers.ignition.GatewayEdition
import com.mussonindustrial.testcontainers.ignition.IgnitionModule
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import java.io.File


abstract class GatewaySettings(objects: ObjectFactory) {
    companion object {
        const val EXTENSION_NAME = "ignitionGateway"
    }

    val acceptLicense: Property<Boolean> = objects.property(Boolean::class.java)
    val gatewayName: Property<String> = objects.property(String::class.java).convention("mussonindustrial-gradle-plugin")
    val username: Property<String> = objects.property(String::class.java).convention("admin")
    val password: Property<String> = objects.property(String::class.java).convention("password")
    val edition: Property<GatewayEdition> = objects.property(GatewayEdition::class.java).convention(GatewayEdition.STANDARD)
    val modules: SetProperty<IgnitionModule> = objects.setProperty(IgnitionModule::class.java).convention(listOf())
    val gatewayBackup: RegularFileProperty = objects.fileProperty()
    val thirdPartyModules: SetProperty<File> = objects.setProperty(File::class.java).convention(listOf())
    val debugMode: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
    val additionalArgs: ListProperty<String> = objects.listProperty(String::class.java).convention(listOf())
    val maxMemory: Property<String> = objects.property(String::class.java)
}

fun StartGateway.applySettings(settings: GatewaySettings) {
    this.acceptLicense.set(settings.acceptLicense)
    this.gatewayName.set(settings.gatewayName)
    this.username.set(settings.username)
    this.password.set(settings.password)
    this.edition.set(settings.edition)
    this.modules.set(settings.modules)
    this.gatewayBackup.set(settings.gatewayBackup)
    this.thirdPartyModules.set(settings.thirdPartyModules)
    this.debugMode.set(settings.debugMode)
    this.additionalArgs.set(settings.additionalArgs)
    this.maxMemory.set(settings.maxMemory)
}