plugins {
    id("embr.build.ignition-module")
}

ignitionModule {
    name.set("Embr SNMP Drivers")
    moduleDescription.set("Drivers for connecting to devices over SNMP.")
    id.set("com.mussonindustrial.embr.snmp")
    fileName.set("Embr-SNMP-Ignition81-${version}.modl")
    freeModule.set(true)
    requiredIgnitionVersion.set(libs.versions.ignition.sdk)
    license.set("license.html")

    projectScopes.putAll(
        mapOf(
            ":modules:snmp:client" to "CD",
            ":modules:snmp:common" to "CGD",
            ":modules:snmp:designer" to "D",
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
            "com.mussonindustrial.embr.snmp.SnmpClientHook" to "C",
            "com.mussonindustrial.embr.snmp.SnmpDesignerHook" to "D",
            "com.mussonindustrial.embr.snmp.SnmpGatewayHook" to "G",
        ),
    )
}
