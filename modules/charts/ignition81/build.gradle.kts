plugins {
    id("embr.ignition-module-conventions")
}

allprojects {
    group = "com.mussonindustrial.embr.thermo"
}

val sdk = libs.ignition.sdk81
val sdkVersion = libs.versions.ignition.sdk81.target
val core = projects.core.ignition81
val module = projects.charts.ignition81

ignitionModule {
    name.set("Embr Charts")
    moduleDescription.set("A collection of enhanced Perspective charting components.")
    id.set("com.mussonindustrial.embr.charts")
    fileName.set("Embr-Charts-Ignition81-${version}.modl")
    freeModule.set(true)
    requiredIgnitionVersion.set(sdkVersion)
    license.set("../license.html")

    projectScopes.putAll(
        mapOf(
            module.common.dependencyProject.path to "GD",
            module.designer.dependencyProject.path to "D",
            module.gateway.dependencyProject.path to "G",
        ),
    )

    moduleDependencies.set(
        mapOf(
            "com.inductiveautomation.perspective" to "GD",
        ),
    )

    hooks.putAll(
        mapOf(
            "com.mussonindustrial.ignition.embr.charts.GatewayHook" to "G",
            "com.mussonindustrial.ignition.embr.charts.DesignerHook" to "D",
        ),
    )
}