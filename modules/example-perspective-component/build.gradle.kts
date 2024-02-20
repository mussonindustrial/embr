import java.util.Date
import java.text.SimpleDateFormat

plugins {
    id("embr.ignition-module-conventions")
}

fun buildTime(): String { return SimpleDateFormat("yyyyMMddHH").format(Date()) }

allprojects {
    group = "com.mussonindustrial.embr"
    version = "0.0.1"
}

ignitionModule {
    name.set("Example Perspective Component")
    moduleDescription.set("Example Perspective Component.")
    id.set("com.mussonindustrial.ignition.perspective.example")
    fileName.set("ExamplePerspectiveComponent.modl")
    moduleVersion.set("${project.version}.${buildTime()}")
    freeModule.set(true)
    requiredIgnitionVersion.set(libs.versions.ignition)

    projectScopes.putAll(
        mapOf(
            ":modules:example-perspective-component:common" to "GD",
            ":modules:example-perspective-component:gateway" to "G",
            ":modules:example-perspective-component:designer" to "D",
        ),
    )

    moduleDependencies.set(
        mapOf(
            "com.inductiveautomation.perspective" to "GD",
        ),
    )

    hooks.putAll(
        mapOf(
            "com.mussonindustrial.ignition.perspective.example.GatewayHook" to "G",
            "com.mussonindustrial.ignition.perspective.example.DesignerHook" to "D",
        ),
    )
}
