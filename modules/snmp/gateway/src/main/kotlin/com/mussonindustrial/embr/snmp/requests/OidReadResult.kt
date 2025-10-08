package com.mussonindustrial.embr.snmp.requests

import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue

data class OidReadResult(val value: DataValue)

fun DataValue.toOidReadResult(): OidReadResult {
    return OidReadResult(this)
}
