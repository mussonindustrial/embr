package com.mussonindustrial.embr.snmp.model

data class BasicOidValue<T>(override val oid: Oid, override val value: T) : OidValue<T>
