
plugins {
    id("embr.ignition-module-conventions")
}

ignitionModule {
    name.set("Embr Charts")
    moduleDescription.set("A collection of enhanced Perspective charting components.")
    id.set("com.mussonindustrial.embr.charts")
    fileName.set("Embr-Charts-${version}.modl")
    freeModule.set(true)
    requiredIgnitionVersion.set(libs.versions.ignition)
    license.set("license.html")

    projectScopes.putAll(
        mapOf(
            ":modules:embr-charts:common" to "GD",
            ":modules:embr-charts:gateway" to "G",
            ":modules:embr-charts:designer" to "D",
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