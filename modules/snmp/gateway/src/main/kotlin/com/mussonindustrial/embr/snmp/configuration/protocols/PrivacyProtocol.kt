package com.mussonindustrial.embr.snmp.configuration.protocols

import org.snmp4j.security.Priv3DES
import org.snmp4j.security.PrivAES128
import org.snmp4j.security.PrivAES192
import org.snmp4j.security.PrivAES256
import org.snmp4j.security.PrivDES
import org.snmp4j.security.nonstandard.PrivAES192With3DESKeyExtension
import org.snmp4j.security.nonstandard.PrivAES256With3DESKeyExtension

enum class PrivacyProtocol(
    val mappedProtocol: org.snmp4j.security.PrivacyProtocol?,
    val prettyName: String,
) {
    NONE(null, "None"),
    DES(PrivDES(), "DES"),
    _3DES(Priv3DES(), "3DES"),
    AES128(PrivAES128(), "AES-128"),
    AES192(PrivAES192(), "AES-192"),
    AES256(PrivAES256(), "AES-256"),
    AES192with3DES(PrivAES192With3DESKeyExtension(), "AES-192 + 3DES Key Extension"),
    AES256with3DES(PrivAES256With3DESKeyExtension(), "AES-256 + 3DES Key Extension");

    override fun toString(): String {
        return prettyName
    }
}
