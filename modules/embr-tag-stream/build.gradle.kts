plugins {
    id("embr.ignition-module-conventions")
}

ignitionModule {
    name.set("Embr Tag Stream")
    moduleDescription.set("Provides an API for streaming tag changes via SSE.")
    id.set("com.mussonindustrial.embr.tagstream")
    fileName.set("Embr-TagStream-module.modl")
    freeModule.set(true)
    requiredIgnitionVersion.set(libs.versions.ignition)

    projectScopes.putAll(
        mapOf(
            ":modules:embr-tag-stream:common" to "GD",
            ":modules:embr-tag-stream:gateway" to "G",
        ),
    )
        hooks.putAll(
        mapOf(
            "com.mussonindustrial.ignition.embr.tagstream.TagStreamGatewayHook" to "G"
        ),
    )
}
