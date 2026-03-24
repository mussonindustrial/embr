plugins {
    id("embr.build.ignition-module")
}

ignitionModule {
    name.set("Embr Thermodynamics")
    moduleDescription.set("Scripting functions for computing thermodynamic properties.")
    id.set("com.mussonindustrial.embr.thermo")
    fileName.set("Embr-Thermodynamics-Ignition81-${version}.modl")
    freeModule.set(false)
    requiredIgnitionVersion.set(libs.versions.ignition.sdk)
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
