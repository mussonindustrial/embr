package com.mussonindustrial.embr.snmp.utils

data class OidPath(val oid: String, val suffix: String? = null)

fun String.parseOidPath(): OidPath? {
    val parts = this.split("::", limit = 2)
    val oid = parts[0]
    val suffix = parts.getOrNull(1)?.takeIf { it.isNotBlank() }
    return OidPath(oid, suffix)
}
