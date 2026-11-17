package com.mussonindustrial.ignition.embr.periscope.resources

class ComponentLibraryResource(
    fileLocations: CompiledResource.FileLocations,
    fileContents: CompiledResource.FileContents,
    compilerMetadata: CompiledResource.CompilerMetadata,
) : AbstractClientResource(fileLocations, fileContents, compilerMetadata) {

    override val descriptor = Descriptor

    companion object Descriptor : ClientResourceDescriptor<ComponentLibraryResource> {
        override val type = ClientResource.Type("component-library")
        override val nounKey = "periscope.client-resource.component-library.noun"
        override val contentType = "application/javascript"
        override val defaultResourceName = "NewComponentLibrary"
        override val defaultFileLocations =
            CompiledResource.FileLocations("source.tsx", "client.js")

        override fun create(
            fileLocations: CompiledResource.FileLocations,
            fileContents: CompiledResource.FileContents,
            compilerMetadata: CompiledResource.CompilerMetadata,
        ): ComponentLibraryResource {
            return ComponentLibraryResource(fileLocations, fileContents, compilerMetadata)
        }
    }
}
