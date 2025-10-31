package com.mussonindustrial.embr.snmp.protocols

import org.snmp4j.security.Priv3DES
import org.snmp4j.security.PrivAES128
import org.snmp4j.security.PrivAES192
import org.snmp4j.security.PrivAES256
import org.snmp4j.security.PrivDES
import org.snmp4j.security.nonstandard.PrivAES192With3DESKeyExtension
import org.snmp4j.security.nonstandard.PrivAES256With3DESKeyExtension

@Suppress("unused")
enum class PrivacyProtocol(
    val mappedProtocol: org.snmp4j.security.PrivacyProtocol?,
    val prettyName: String,
) {
    None(null, "None"),
    Des(PrivDES(), "DES"),
    Tdes(Priv3DES(), "3DES/TDES/TDEA"),
    Aes128(PrivAES128(), "AES-128"),
    Aes192(PrivAES192(), "AES-192"),
    Aes256(PrivAES256(), "AES-256"),
    Aes192Tdes(PrivAES192With3DESKeyExtension(), "AES-192 + 3DES/TDES/TDEA Key Extension"),
    Aes256Tdes(PrivAES256With3DESKeyExtension(), "AES-256 + 3DES/TDES/TDEA Key Extension");

    override fun toString(): String {
        return prettyName
    }
}
