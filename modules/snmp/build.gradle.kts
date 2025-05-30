plugins {
    id("embr.ignition-module-conventions")
}

ignitionModule {
    name.set("Embr SNMP Driver")
    moduleDescription.set("Driver for connecting to devices over SNMP.")
    id.set("com.mussonindustrial.embr.snmp")
    fileName.set("Embr-SNMP-${version}.modl")
    freeModule.set(true)
    requiredIgnitionVersion.set(libs.versions.ignition)
    license.set("license.html")

    projectScopes.putAll(
        mapOf(
            ":modules:snmp:common" to "CGD",
            ":modules:snmp:gateway" to "G",
        ),
    )

    moduleDependencySpecs {
        register("com.inductiveautomation.opcua") {
            required = true
            scope = "G"
        }
    }

    hooks.putAll(
        mapOf(
            "com.mussonindustrial.embr.snmp.SnmpGatewayHook" to "G",
        ),
    )
}
