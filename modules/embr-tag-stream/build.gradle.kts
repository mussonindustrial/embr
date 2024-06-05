import java.util.Date
import java.text.SimpleDateFormat
plugins {
    id("embr.ignition-module-conventions")
}

fun buildTime(): String { return SimpleDateFormat("yyyyMMddHH").format(Date()) }

allprojects {
    group = "com.mussonindustrial.embr"
}

ignitionModule {
    name.set("Embr Tag Stream")
    moduleDescription.set("Provides an API for streaming tag changes via SSE.")
    id.set("com.mussonindustrial.embr.sse")
    fileName.set("Embr-TagStream-module.modl")
    moduleVersion.set("${project.version}.${buildTime()}")
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
            "com.mussonindustrial.ignition.embr.sse.GatewayHook" to "G"
        ),
    )
}
