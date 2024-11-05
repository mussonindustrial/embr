
plugins {
    id("embr.ignition-module-conventions")
}

ignitionModule {
    name.set("Embr Periscope")
    moduleDescription.set("Design extensions and enhancements for Perspective.")
    id.set("com.mussonindustrial.embr.periscope")
    fileName.set("Embr-Periscope-${version}.modl")
    freeModule.set(true)
    requiredIgnitionVersion.set(libs.versions.ignition)
    license.set("license.html")

    projectScopes.putAll(
        mapOf(
            ":modules:periscope:common" to "GD",
            ":modules:periscope:gateway" to "G",
            ":modules:periscope:designer" to "D",
        ),
    )

    moduleDependencies.set(
        mapOf(
            "com.inductiveautomation.perspective" to "GD",
        ),
    )

    hooks.putAll(
        mapOf(
            "com.mussonindustrial.ignition.embr.periscope.GatewayHook" to "G",
            "com.mussonindustrial.ignition.embr.periscope.DesignerHook" to "D",
        ),
    )
}