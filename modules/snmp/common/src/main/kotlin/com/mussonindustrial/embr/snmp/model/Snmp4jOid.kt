package com.mussonindustrial.embr.snmp.model

import org.snmp4j.smi.OID

class Snmp4jOid(val oid: OID) : OID(oid), Oid {

    constructor(numeric: String) : this(OID(numeric))

    override val numeric: String
        get() = oid.toDottedString()

    override val symbolicName: String
        get() = "unknownSymbol[$numeric]"

    override val index: Int
        get() = OID(oid).removeLast()

    override val parent: Snmp4jOid
        get() {
            val parentOid = OID(oid)
            parentOid.removeLast()
            return Snmp4jOid(parentOid)
        }

    override fun toString(): String = numeric
}

fun OID.toOid(): Snmp4jOid = Snmp4jOid(this)

fun Oid.toSnmp4j(): OID = Snmp4jOid(numeric)
