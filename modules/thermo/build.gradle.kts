plugins {
    id("embr.ignition-module-conventions")
}

ignitionModule {
    name.set("Embr Thermodynamics")
    moduleDescription.set("Scripting functions for computing thermodynamic properties.")
    id.set("com.mussonindustrial.embr.thermo")
    fileName.set("Embr-Thermodynamics-Ignition83-${version}.modl")
    freeModule.set(true)
    requiredIgnitionVersion.set("8.3.0")
    license.set("license.html")

    projectScopes.putAll(
        mapOf(
            ":modules:thermo:common" to "CGD",
            ":modules:thermo:client" to "C",
            ":modules:thermo:designer" to "D",
            ":modules:thermo:gateway" to "G",
        ),
    )

    hooks.putAll(
        mapOf(
            "com.mussonindustrial.embr.thermo.ThermoClientHook" to "C",
            "com.mussonindustrial.embr.thermo.ThermoDesignerHook" to "D",
            "com.mussonindustrial.embr.thermo.ThermoGatewayHook" to "G",
        ),
    )
}
