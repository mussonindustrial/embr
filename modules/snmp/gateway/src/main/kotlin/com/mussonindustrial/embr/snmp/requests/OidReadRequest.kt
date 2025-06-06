package com.mussonindustrial.embr.snmp.requests

import org.snmp4j.smi.OID

interface OidReadRequest {
    val oid: OID
    var result: OidReadResult?
}
