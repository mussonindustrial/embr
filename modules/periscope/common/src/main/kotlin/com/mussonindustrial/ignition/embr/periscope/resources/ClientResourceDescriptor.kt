package com.mussonindustrial.ignition.embr.periscope.resources

import com.inductiveautomation.ignition.common.project.resource.ProjectResource
import javax.swing.Icon

interface ClientResourceDescriptor<T : ClientResource> {

    val type: ClientResource.Type
    val contentType: String
    val defaultFileLocations: CompiledResource.FileLocations

    val icon: Icon?
        get() = null

    val nounKey: String
    val nounPluralKey: String
        get() = nounKey + "s"

    val nounLongKey: String
        get() = "$nounKey-long"

    val nounLongPluralKey: String
        get() = nounLongKey + "s"

    val defaultResourceName: String

    fun createEmpty(): T {
        return create(
            defaultFileLocations,
            CompiledResource.FileContents.EMPTY,
            CompiledResource.CompilerMetadata.EMPTY,
        )
    }

    fun create(
        fileLocations: CompiledResource.FileLocations,
        fileContents: CompiledResource.FileContents,
        compilerMetadata: CompiledResource.CompilerMetadata,
    ): T

    fun fromResource(resource: ProjectResource): T {
        val locations = CompiledResource.FileLocations.fromResource(resource)
        val contents = CompiledResource.FileContents.fromResource(resource)
        val compilerMetadata = CompiledResource.CompilerMetadata.fromResource(resource)
        return create(locations, contents, compilerMetadata)
    }
}
