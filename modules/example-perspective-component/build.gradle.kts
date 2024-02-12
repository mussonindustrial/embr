import java.util.Date
import java.text.SimpleDateFormat

plugins {
    id("embr.ignition-module-conventions")
}

fun buildTime(): String { return SimpleDateFormat("yyyyMMddHH").format(Date()) }

tasks {
    named("clean") {
        dependsOn(":gateway:clean", ":common:clean", ":designer:clean")
    }
}

allprojects {
    version = "${project.version}.${buildTime()}"
}

ignitionModule {
    name.set("Example Perspective Component")
    fileName.set("ExamplePerspectiveComponent.modl")
    id.set("com.mussonindustrial.ignition.perspective.example")
    moduleVersion.set("${project.version}")
    freeModule.set(true)

    moduleDescription.set("Example Perspective Component.")
    requiredIgnitionVersion.set(libs.versions.ignition)

    projectScopes.putAll(
        mapOf(
            ":common" to "GD",
            ":gateway" to "G",
            ":designer" to "D",
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
