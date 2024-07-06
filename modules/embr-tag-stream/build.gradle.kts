plugins {
    id("embr.ignition-module-conventions")
}

ignitionModule {
    name.set("Embr Event Stream")
    moduleDescription.set("Provides an API for streaming events via SSE.")
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

    moduleDependencies.set(
        mapOf(
            "com.inductiveautomation.perspective" to "G"
        ),
    )

    hooks.putAll(
        mapOf(
            "com.mussonindustrial.ignition.embr.tagstream.EventStreamGatewayHook" to "G"
        ),
    )
}
