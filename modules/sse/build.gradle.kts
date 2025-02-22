plugins {
    id("embr.ignition-module-conventions")
}

ignitionModule {
    name.set("Embr Server Sent Events")
    moduleDescription.set("Provides an API for streaming events via SSE.")
    id.set("com.mussonindustrial.embr.sse")
    fileName.set("Embr-SSE-${version}.modl")
    freeModule.set(true)
    requiredIgnitionVersion.set(libs.versions.ignition)

    projectScopes.putAll(
        mapOf(
            ":modules:sse:common" to "G",
            ":modules:sse:gateway" to "G",
        ),
    )

    moduleDependencies.set(
        mapOf(
            "com.inductiveautomation.perspective" to "G",
        ),
    )

    hooks.putAll(
        mapOf(
            "com.mussonindustrial.embr.sse.EventStreamGatewayHook" to "G",
        ),
    )
}
