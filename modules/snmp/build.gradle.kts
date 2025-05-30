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
            ":modules:snmp:client" to "C",
            ":modules:snmp:designer" to "D",
            ":modules:snmp:gateway" to "G",
        ),
    )

    moduleDependencies.set(
        mapOf(
            "com.inductiveautomation.opcua" to "G",
        ),
    )

    hooks.putAll(
        mapOf(
            "com.mussonindustrial.embr.snmp.SnmpClientHook" to "C",
            "com.mussonindustrial.embr.snmp.SnmpDesignerHook" to "D",
            "com.mussonindustrial.embr.snmp.SnmpGatewayHook" to "G",
        ),
    )
}
