plugins {
    id("embr.ignition-module-conventions")
}

ignitionModule {
    name.set("Chart.js Components")
    moduleDescription.set("Simple yet flexible JavaScript charting library for the modern web.")
    id.set("com.mussonindustrial.embr.chartjs")
    fileName.set("Embr-Chartjs-module.modl")
    freeModule.set(true)
    requiredIgnitionVersion.set(libs.versions.ignition)

    projectScopes.putAll(
        mapOf(
            ":modules:embr-chart-js:common" to "GD",
            ":modules:embr-chart-js:gateway" to "G",
            ":modules:embr-chart-js:designer" to "D",
        ),
    )

    moduleDependencies.set(
        mapOf(
            "com.inductiveautomation.perspective" to "GD",
        ),
    )

    hooks.putAll(
        mapOf(
            "com.mussonindustrial.ignition.chartjs.GatewayHook" to "G",
            "com.mussonindustrial.ignition.chartjs.DesignerHook" to "D",
        ),
    )
}
