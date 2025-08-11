plugins {
    id("embr.ignition-module-conventions")
}

ignitionModule {
    name.set("Embr Server Sent Events")
    moduleDescription.set("Provides an API for streaming events via SSE.")
    id.set("com.mussonindustrial.embr.sse")
    fileName.set("Embr-SSE-Ignition81-${version}.modl")
    freeModule.set(true)
    requiredIgnitionVersion.set(libs.versions.ignition)

    projectScopes.putAll(
        mapOf(
            ":modules:sse:gateway" to "G",
        ),
    )

    moduleDependencySpecs {
        register("com.inductiveautomation.perspective") {
            scope = "G"
            required = false
        }
    }

    hooks.putAll(
        mapOf(
            "com.mussonindustrial.embr.sse.EventStreamGatewayHook" to "G",
        ),
    )
}
