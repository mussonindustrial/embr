package com.mussonindustrial.ignition.embr.periscope.resources

import com.inductiveautomation.ignition.common.project.resource.ProjectResource

class ClientResourceManager {

    val descriptorsByType =
        mutableMapOf<ClientResource.Type, ClientResourceDescriptor<out ClientResource>>()

    val types
        get() = descriptorsByType.keys.toList()

    val descriptors
        get() = descriptorsByType.values.toList()

    fun register(definition: ClientResourceDescriptor<out ClientResource>) {
        descriptorsByType[definition.type] = definition
    }

    fun fromResource(resource: ProjectResource): ClientResource? {
        val type = ClientResource.getType(resource) ?: return null
        val definition = descriptorsByType[type] ?: return null
        return definition.fromResource(resource)
    }

    fun getDescriptor(resource: ProjectResource): ClientResourceDescriptor<out ClientResource>? {
        val type = ClientResource.getType(resource) ?: return null
        return descriptorsByType[type]
    }
}
