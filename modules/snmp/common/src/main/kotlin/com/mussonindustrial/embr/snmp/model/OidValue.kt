package com.mussonindustrial.embr.snmp.model

interface OidValue<T> {
    val oid: Oid
    val value: T
}
