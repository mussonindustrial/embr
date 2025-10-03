package com.mussonindustrial.embr.snmp.configuration.protocols

import org.snmp4j.security.AuthGeneric
import org.snmp4j.security.AuthHMAC128SHA224
import org.snmp4j.security.AuthHMAC256SHA384
import org.snmp4j.security.AuthHMAC384SHA512
import org.snmp4j.security.AuthMD5
import org.snmp4j.security.AuthSHA

enum class AuthenticationProtocol(val mappedProtocol: AuthGeneric?, val prettyName: String) {
    NONE(null, "None"),
    MD5(AuthMD5(), "MD-5"),
    SHA1(AuthSHA(), "SHA-1"),
    SHA224(AuthHMAC128SHA224(), "SHA-224"),
    SHA256(AuthHMAC128SHA224(), "SHA-256"),
    SHA384(AuthHMAC256SHA384(), "SHA-384"),
    SHA512(AuthHMAC384SHA512(), "SHA-512");

    override fun toString(): String {
        return prettyName
    }
}
