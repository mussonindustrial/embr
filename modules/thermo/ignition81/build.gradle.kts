plugins {
    id("embr.ignition-module-conventions")
}

allprojects {
    group = "com.mussonindustrial.embr.thermo"
}

val sdk = libs.ignition.sdk81
val sdkVersion = libs.versions.ignition.sdk81.target
val core = projects.core.ignition81
val module = projects.thermo.ignition81

ignitionModule {
    name.set("Embr Thermodynamics")
    moduleDescription.set("Scripting functions for computing thermodynamic properties.")
    id.set("com.mussonindustrial.embr.thermo")
    fileName.set("Embr-Thermodynamics-Ignition81-${version}.modl")
    freeModule.set(true)
    requiredIgnitionVersion.set(sdkVersion)
    license.set("../license.html")

    projectScopes.putAll(
        mapOf(
            module.common.dependencyProject.path to "CGD",
            module.client.dependencyProject.path to "C",
            module.designer.dependencyProject.path to "D",
            module.gateway.dependencyProject.path to "G",
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

