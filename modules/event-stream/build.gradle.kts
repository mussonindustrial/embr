plugins {
    id("embr.ignition-module-conventions")
}

ignitionModule {
    name.set("Embr Event Stream")
    moduleDescription.set("Provides an API for streaming events via SSE.")
    id.set("com.mussonindustrial.embr.eventstream")
    fileName.set("Embr-EventStream-${version}.modl")
    freeModule.set(true)
    requiredIgnitionVersion.set(libs.versions.ignition)

    projectScopes.putAll(
        mapOf(
            ":modules:event-stream:common" to "G",
            ":modules:event-stream:gateway" to "G",
        ),
    )

    moduleDependencies.set(
        mapOf(
            "com.inductiveautomation.perspective" to "G",
        ),
    )

    hooks.putAll(
        mapOf(
            "com.mussonindustrial.embr.eventstream.EventStreamGatewayHook" to "G",
        ),
    )
}
