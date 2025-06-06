package com.mussonindustrial.embr.snmp.utils

import java.text.ParseException
import org.eclipse.milo.opcua.sdk.server.Lifecycle
import org.eclipse.milo.opcua.sdk.server.LifecycleManager
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant
import org.snmp4j.PDU
import org.snmp4j.SNMP4JSettings
import org.snmp4j.Snmp
import org.snmp4j.smi.OID
import org.snmp4j.smi.OctetString
import org.snmp4j.smi.Variable
import org.snmp4j.smi.VariableBinding

fun String.toVariableBinding(): VariableBinding {
    return VariableBinding(OID(this))
}

fun PDU.addOID(oid: String) {
    val binding = VariableBinding(OID(oid))
    this.add(binding)
}

fun String.isOid(): Boolean {
    try {
        SNMP4JSettings.getOIDTextFormat().parse(this)
        return true
    } catch (_: ParseException) {
        return false
    }
}

fun Variable.toDataValue(): DataValue {
    return DataValue(Variant("TODO - Implement Variable to DataValue mapping."))
}

fun DataValue.toVariable(): Variable {
    return OctetString(this.value.value.toString())
}

fun LifecycleManager.addLifecycle(snmp: Snmp) {
    this.addLifecycle(
        object : Lifecycle {
            override fun startup() {
                snmp.listen()
            }

            override fun shutdown() {
                snmp.close()
            }
        }
    )
}
