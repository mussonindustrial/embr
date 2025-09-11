package com.mussonindustrial.ignition.embr.e2e.env

import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

open class IgnitionConfig @Inject constructor(objects: ObjectFactory) {
    val dockerImage: Property<String> = objects.property(String::class.java)
    val gatewayBackup: RegularFileProperty = objects.fileProperty()
    val modulesDir: DirectoryProperty = objects.directoryProperty()
}

open class PlaywrightConfig @Inject constructor(objects: ObjectFactory) {
    val dockerImage: Property<String> = objects.property(String::class.java)
}

open class TestEnvironmentExtension @Inject constructor(objects: ObjectFactory) {
    val ignition: IgnitionConfig = objects.newInstance(IgnitionConfig::class.java)
    val playwright: PlaywrightConfig = objects.newInstance(PlaywrightConfig::class.java)

    fun ignition(action: Action<IgnitionConfig>) {
        action.execute(ignition)
    }

    fun playwright(action: Action<PlaywrightConfig>) {
        action.execute(playwright)
    }
}
