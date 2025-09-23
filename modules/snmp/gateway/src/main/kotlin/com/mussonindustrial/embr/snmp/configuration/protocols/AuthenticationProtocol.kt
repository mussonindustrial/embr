package com.mussonindustrial.embr.snmp.configuration.protocols

import com.inductiveautomation.ignition.gateway.dataroutes.openapi.annotations.Enumeration
import org.snmp4j.fluent.TargetBuilder

enum class AuthenticationProtocol(
    val mappedProtocol: TargetBuilder.AuthProtocol,
    val prettyName: String,
) {
    MD5(TargetBuilder.AuthProtocol.md5, "MD-5"),
    SHA1(TargetBuilder.AuthProtocol.sha1, "SHA-1"),
    SHA224(TargetBuilder.AuthProtocol.hmac128sha224, "SHA-224"),
    SHA256(TargetBuilder.AuthProtocol.hmac192sha256, "SHA-256"),
    SHA384(TargetBuilder.AuthProtocol.hmac256sha384, "SHA-384"),
    SHA512(TargetBuilder.AuthProtocol.hmac384sha512, "SHA-512");

    override fun toString(): String {
        return prettyName
    }

    class Provider : Enumeration.Provider {
        override fun values(): Array<out Any> {
            return AuthenticationProtocol.entries.toTypedArray()
        }
    }
}
