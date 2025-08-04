
plugins {
    id("embr.ignition-module-conventions")
}

ignitionModule {
    name.set("Embr Periscope")
    moduleDescription.set("Design extensions and enhancements for Perspective.")
    id.set("com.mussonindustrial.embr.periscope")
    fileName.set("Embr-Periscope-Ignition83Beta-${version}.modl")
    freeModule.set(true)
    requiredIgnitionVersion.set("8.3.0")
    license.set("license.html")

    projectScopes.putAll(
        mapOf(
            ":modules:periscope:common" to "GD",
            ":modules:periscope:gateway" to "G",
            ":modules:periscope:designer" to "D",
        ),
    )

    moduleDependencySpecs {
        register("com.inductiveautomation.perspective") {
            scope = "GD"
            required = true
        }
    }

    hooks.putAll(
        mapOf(
            "com.mussonindustrial.ignition.embr.periscope.PeriscopeGatewayHook" to "G",
            "com.mussonindustrial.ignition.embr.periscope.PeriscopeDesignerHook" to "D",
        ),
    )
}