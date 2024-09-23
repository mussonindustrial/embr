plugins {
    id("embr.ignition-module-conventions")
}

allprojects {
    group = "com.mussonindustrial.embr.sse"
}

val sdk = libs.ignition.sdk81
val sdkVersion = libs.versions.ignition.sdk81.target
val core = projects.libraries.core.ignition81
val module = projects.modules.sse.ignition81

ignitionModule {
    name.set("Embr Server Sent Events")
    moduleDescription.set("Provides an API for streaming events via SSE.")
    id.set("com.mussonindustrial.embr.eventstream")
    fileName.set("Embr-Ignition81-SSE-${version}.modl")
    freeModule.set(true)
    requiredIgnitionVersion.set(sdkVersion)

    projectScopes.putAll(
        mapOf(
            module.gateway.dependencyProject.path to "G",
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
