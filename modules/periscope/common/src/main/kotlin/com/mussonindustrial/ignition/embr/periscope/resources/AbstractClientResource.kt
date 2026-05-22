package com.mussonindustrial.ignition.embr.periscope.resources

abstract class AbstractClientResource(
    override val fileLocations: CompiledResource.FileLocations,
    override val fileContents: CompiledResource.FileContents,
    override val compilerMetadata: CompiledResource.CompilerMetadata,
) : ClientResource
