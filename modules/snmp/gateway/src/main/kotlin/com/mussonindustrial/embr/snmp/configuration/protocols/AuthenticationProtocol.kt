package com.mussonindustrial.embr.snmp.configuration.protocols

import org.snmp4j.security.AuthGeneric
import org.snmp4j.security.AuthHMAC128SHA224
import org.snmp4j.security.AuthHMAC192SHA256
import org.snmp4j.security.AuthHMAC256SHA384
import org.snmp4j.security.AuthHMAC384SHA512
import org.snmp4j.security.AuthMD5
import org.snmp4j.security.AuthSHA

@Suppress("unused")
enum class AuthenticationProtocol(val mappedProtocol: AuthGeneric?, val prettyName: String) {
    None(null, "None"),
    Md5(AuthMD5(), "MD-5"),
    Sha1(AuthSHA(), "SHA-1"),
    Sha224(AuthHMAC128SHA224(), "SHA-224"),
    Sha256(AuthHMAC192SHA256(), "SHA-256"),
    Sha384(AuthHMAC256SHA384(), "SHA-384"),
    Sha512(AuthHMAC384SHA512(), "SHA-512");

    override fun toString(): String {
        return prettyName
    }
}
