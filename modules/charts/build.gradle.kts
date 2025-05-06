
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
            ":modules:charts:common" to "GD",
            ":modules:charts:gateway" to "G",
            ":modules:charts:designer" to "D",
        ),
    )

    moduleDependencies.set(
        mapOf(
            "com.inductiveautomation.perspective" to "GD",
            "com.kyvislabs.apexcharts" to "GD",
        ),
    )

    hooks.putAll(
        mapOf(
            "com.mussonindustrial.ignition.embr.charts.ChartsGatewayHook" to "G",
            "com.mussonindustrial.ignition.embr.charts.ChartsDesignerHook" to "D",
        ),
    )
}