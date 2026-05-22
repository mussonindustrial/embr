package com.mussonindustrial.ignition.embr.periscope.resources

import com.inductiveautomation.ignition.common.project.resource.ProjectResource
import com.inductiveautomation.ignition.common.project.resource.ProjectResourceBuilder
import com.inductiveautomation.ignition.common.project.resource.ResourceType
import com.mussonindustrial.ignition.embr.periscope.Meta

sealed interface ClientResource : CompiledResource {

    @JvmInline value class Type(val key: String)

    val descriptor: ClientResourceDescriptor<out ClientResource>

    val type: Type
        get() = descriptor.type

    val contentType: String
        get() = descriptor.contentType

    companion object {
        const val RESOURCE_TYPE_KEY = "type"
        val type = ResourceType(Meta.MODULE_ID, "client-resource")

        fun getType(resource: ProjectResource): Type? =
            resource.getAttribute(RESOURCE_TYPE_KEY).map { Type(it.asString) }.orElse(null)
    }

    override fun applyToBuilder(builder: ProjectResourceBuilder) {
        super.applyToBuilder(builder)
        builder.putAttribute(RESOURCE_TYPE_KEY, type.key)
    }
}
