package com.mussonindustrial.embr.snmp.requests

import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue
import org.snmp4j.smi.OID

interface OidWriteRequest {
    val oid: OID
    val value: DataValue
    var result: OidWriteResult?
}
