package com.mussonindustrial.ignition.embr.periscope.resources

import com.inductiveautomation.ignition.common.resourcecollection.Resource
import com.inductiveautomation.ignition.common.resourcecollection.ResourceBuilder
import kotlin.jvm.optionals.getOrNull

class ComponentResource(
    fileLocations: CompiledResource.FileLocations,
    fileContents: CompiledResource.FileContents,
    compilerMetadata: CompiledResource.CompilerMetadata,
    val attributes: ComponentAttributes = ComponentAttributes.EMPTY,
    val delegate: String = "",
) : AbstractClientResource(fileLocations, fileContents, compilerMetadata) {

    override val descriptor = Descriptor

    companion object Descriptor : ClientResourceDescriptor<ComponentResource> {
        override val type = ClientResource.Type("component")
        override val nounKey = "periscope.client-resource.component.noun"
        override val contentType = "application/javascript"
        override val defaultResourceName = "NewComponent"
        const val DELEGATE_FILE = "delegate.py"
        const val DELEGATE_FACTORY = "create"

        override val defaultFileLocations =
            CompiledResource.FileLocations("source.tsx", "client.js")

        private val DEFAULT_SOURCE =
            """
            import * as React from 'react'
            import { defineComponent, prop } from 'system:periscope'

            export default defineComponent({
              props: {
                text: prop.string().default('Component'),
              },
              render: ({ text }, { Root }) => <Root>{text}</Root>,
            })
            """
                .trimIndent() + "\n"

        override fun create(
            fileLocations: CompiledResource.FileLocations,
            fileContents: CompiledResource.FileContents,
            compilerMetadata: CompiledResource.CompilerMetadata,
        ): ComponentResource {
            return ComponentResource(fileLocations, fileContents, compilerMetadata)
        }

        override fun createEmpty(): ComponentResource {
            return create(
                defaultFileLocations,
                CompiledResource.FileContents(DEFAULT_SOURCE, ""),
                CompiledResource.CompilerMetadata.EMPTY,
            )
        }

        override fun fromResource(resource: Resource): ComponentResource {
            return super.fromResource(resource)
                .withAttributes(ComponentAttributes.fromResource(resource))
                .withDelegate(getDelegate(resource))
        }

        fun getDelegate(resource: Resource): String {
            return resource.getData(DELEGATE_FILE).getOrNull()?.bytesAsString.orEmpty()
        }
    }

    fun withAttributes(attributes: ComponentAttributes): ComponentResource {
        return ComponentResource(
            fileLocations,
            fileContents,
            compilerMetadata,
            attributes,
            delegate,
        )
    }

    fun withDelegate(delegate: String): ComponentResource {
        return ComponentResource(
            fileLocations,
            fileContents,
            compilerMetadata,
            attributes,
            delegate,
        )
    }

    override fun applyToBuilder(builder: ResourceBuilder) {
        super.applyToBuilder(builder)
        attributes.accept(builder)

        if (delegate.isBlank()) {
            builder.removeData(DELEGATE_FILE)
        } else {
            builder.putData(DELEGATE_FILE, delegate.encodeToByteArray())
        }
    }
}
