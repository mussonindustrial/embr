package com.mussonindustrial.embr.snmp.model

import java.text.ParseException
import org.snmp4j.SNMP4JSettings
import org.snmp4j.smi.OID

interface ExtendedOid : Oid {
    val suffix: String?

    val hasSuffix: Boolean
        get() = suffix != null
}

fun String.isOid(): Boolean {
    return try {
        this.asExtendedOid()
        true
    } catch (_: ParseException) {
        false
    }
}

fun String.asExtendedOid(): ExtendedOid {
    val parts = this.split("::", limit = 2)
    val maybeOid = parts[0]
    val suffix = parts.getOrNull(1)?.takeIf { it.isNotBlank() }

    val oid = SNMP4JSettings.getOIDTextFormat().parse(maybeOid)
    return Snmp4jExtendedOid(OID(oid), suffix)
}

fun String?.nullOrExtendedOid(): ExtendedOid? {
    this?.let {
        return this.asExtendedOid()
    }
    return null
}
