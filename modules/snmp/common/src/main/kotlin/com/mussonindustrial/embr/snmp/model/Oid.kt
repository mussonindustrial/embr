package com.mussonindustrial.embr.snmp.model

import org.snmp4j.smi.OID

class Oid private constructor(private val oid: OID) {

    companion object {
        fun fromNumeric(dotted: String): Oid = Oid(oid = OID(dotted))

        fun fromSnmp4j(oid: OID): Oid = Oid(oid = OID(oid))
    }

    val numeric: String
        get() = oid.toDottedString()

    @Suppress("UNUSED")
    val dottedString: String
        get() = numeric

    @Suppress("UNUSED")
    fun toDottedString(): String {
        return numeric
    }

    val index: Int
        get() = OID(oid).removeLast()

    val parent: Oid
        get() {
            val parentOid = OID(oid)
            parentOid.removeLast()
            return Oid(oid = parentOid)
        }

    override fun toString(): String = numeric

    override fun equals(other: Any?): Boolean = other is Oid && oid == other.oid

    override fun hashCode(): Int = oid.hashCode()
}
