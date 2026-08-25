package com.mussonindustrial.embr.snmp.model

interface Oid {
    val numeric: String
    val symbolicName: String

    val index: Int
    val parent: Oid
}
