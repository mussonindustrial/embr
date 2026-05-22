package com.mussonindustrial.ignition.embr.periscope.resources

import com.inductiveautomation.ignition.common.resourcecollection.Resource
import com.inductiveautomation.ignition.common.resourcecollection.ResourceBuilder
import com.inductiveautomation.ignition.common.resourcecollection.ResourceType
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

        fun getType(resource: Resource): Type? =
            resource.getAttribute(RESOURCE_TYPE_KEY).map { Type(it.asString) }.orElse(null)
    }

    override fun applyToBuilder(builder: ResourceBuilder) {
        super.applyToBuilder(builder)
        builder.putAttribute(RESOURCE_TYPE_KEY, type.key)
    }
}
