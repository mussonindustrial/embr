
plugins {
    id("embr.ignition-module-conventions")
}

ignitionModule {
    name.set("Embr Mui Joy")
    moduleDescription.set("Mui Joy components Perspective.")
    id.set("com.mussonindustrial.embr.muijoy")
    fileName.set("Embr-MuiJoy-Ignition83-${version}.modl")
    freeModule.set(true)
    requiredIgnitionVersion.set("8.3.0")
    license.set("license.html")

    projectScopes.putAll(
        mapOf(
            ":modules:muijoy:common" to "GD",
            ":modules:muijoy:gateway" to "G",
            ":modules:muijoy:designer" to "D",
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
            "com.mussonindustrial.ignition.embr.muijoy.MuiJoyGatewayHook" to "G",
            "com.mussonindustrial.ignition.embr.muijoy.MuiJoyDesignerHook" to "D",
        ),
    )
}