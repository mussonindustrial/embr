package com.mussonindustrial.ignition.embr.periscope.resources

class CssModuleResource(
    fileLocations: CompiledResource.FileLocations,
    fileContents: CompiledResource.FileContents,
    compilerMetadata: CompiledResource.CompilerMetadata,
) : AbstractClientResource(fileLocations, fileContents, compilerMetadata) {

    override val descriptor = Descriptor

    companion object Descriptor : ClientResourceDescriptor<CssModuleResource> {
        override val type = ClientResource.Type("css")
        override val nounKey = "periscope.client-resource.css.noun"
        override val contentType = "application/javascript"
        override val defaultResourceName = "NewCssModule"
        override val defaultFileLocations =
            CompiledResource.FileLocations("styles.css", "client.js")

        override fun create(
            fileLocations: CompiledResource.FileLocations,
            fileContents: CompiledResource.FileContents,
            compilerMetadata: CompiledResource.CompilerMetadata,
        ): CssModuleResource {
            return CssModuleResource(fileLocations, fileContents, compilerMetadata)
        }
    }
}
