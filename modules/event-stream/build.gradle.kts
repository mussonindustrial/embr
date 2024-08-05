plugins {
    id("embr.ignition-module-conventions")
}

ignitionModule {
    name.set("Embr Event Stream")
    moduleDescription.set("Provides an API for streaming tag changes via SSE.")
    id.set("com.mussonindustrial.embr.eventstream")
    fileName.set("Embr-EventStream-${version}.modl")
    freeModule.set(true)
    requiredIgnitionVersion.set(libs.versions.ignition)

    projectScopes.putAll(
        mapOf(
            ":modules:event-stream:common" to "GD",
            ":modules:event-stream:gateway" to "G",
        ),
    )
        hooks.putAll(
        mapOf(
            "com.mussonindustrial.ignition.embr.tagstream.TagStreamGatewayHook" to "G"
        ),
    )
}
