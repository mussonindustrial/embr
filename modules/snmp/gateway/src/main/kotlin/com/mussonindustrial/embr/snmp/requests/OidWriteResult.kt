package com.mussonindustrial.embr.snmp.requests

import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode
import org.snmp4j.smi.OID

data class OidWriteResult(val oid: OID, val statusCode: StatusCode)

fun OID.toOidWriteResult(statusCode: StatusCode): OidWriteResult {
    return OidWriteResult(this, statusCode)
}
