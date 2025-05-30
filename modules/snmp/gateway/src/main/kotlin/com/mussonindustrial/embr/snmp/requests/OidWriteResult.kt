package com.mussonindustrial.embr.snmp.requests

import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode

data class OidWriteResult(val statusCode: StatusCode)

fun StatusCode.toOidWriteResult(): OidWriteResult {
    return OidWriteResult(this)
}
