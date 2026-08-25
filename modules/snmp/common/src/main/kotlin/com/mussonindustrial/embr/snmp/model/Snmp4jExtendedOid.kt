package com.mussonindustrial.embr.snmp.model

import org.snmp4j.smi.OID

class Snmp4jExtendedOid(oid: OID, override val suffix: String?) : Snmp4jOid(oid), ExtendedOid {
    constructor(numeric: String, suffix: String) : this(OID(numeric), suffix)
}
