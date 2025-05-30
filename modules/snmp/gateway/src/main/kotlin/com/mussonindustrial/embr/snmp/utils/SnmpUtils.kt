package com.mussonindustrial.embr.snmp.utils

import org.snmp4j.PDU
import org.snmp4j.smi.OID
import org.snmp4j.smi.VariableBinding

fun String.toVariableBinding(): VariableBinding {
    return VariableBinding(OID(this))
}

fun PDU.addOID(oid: String) {
    val binding = VariableBinding(OID(oid))
    this.add(binding)
}
