package com.mussonindustrial.ignition.embr.periscope.resources

class TypeScriptResource(
    fileLocations: CompiledResource.FileLocations,
    fileContents: CompiledResource.FileContents,
    compilerMetadata: CompiledResource.CompilerMetadata,
) : AbstractClientResource(fileLocations, fileContents, compilerMetadata) {

    override val descriptor = Descriptor

    companion object Descriptor : ClientResourceDescriptor<TypeScriptResource> {
        override val type = ClientResource.Type("typescript")
        override val nounKey = "periscope.client-resource.typescript.noun"
        override val contentType = "application/javascript"
        override val defaultResourceName = "NewTypeScriptModule"
        override val defaultFileLocations =
            CompiledResource.FileLocations("source.tsx", "client.js")

        override fun create(
            fileLocations: CompiledResource.FileLocations,
            fileContents: CompiledResource.FileContents,
            compilerMetadata: CompiledResource.CompilerMetadata,
        ): TypeScriptResource {
            return TypeScriptResource(fileLocations, fileContents, compilerMetadata)
        }
    }
}
