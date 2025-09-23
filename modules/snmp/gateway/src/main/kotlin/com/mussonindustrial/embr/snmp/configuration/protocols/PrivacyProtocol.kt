package com.mussonindustrial.embr.snmp.configuration.protocols

import com.inductiveautomation.ignition.gateway.dataroutes.openapi.annotations.Enumeration
import org.snmp4j.fluent.TargetBuilder

enum class PrivacyProtocol(val mappedProtocol: TargetBuilder.PrivProtocol, val prettyName: String) {
    DES(TargetBuilder.PrivProtocol.des, "DES"),
    _3DES(TargetBuilder.PrivProtocol._3des, "3DES"),
    AES128(TargetBuilder.PrivProtocol.aes128, "AES-128"),
    AES192(TargetBuilder.PrivProtocol.aes192, "AES-192"),
    AES256(TargetBuilder.PrivProtocol.aes256, "AES-256"),
    AES192with3DES(
        TargetBuilder.PrivProtocol.aes192with3DESKeyExtension,
        "AES-192 + 3DES Key Extension",
    ),
    AES256with3DES(
        TargetBuilder.PrivProtocol.aes256with3DESKeyExtension,
        "AES-256 + 3DES Key Extension",
    );

    override fun toString(): String {
        return prettyName
    }

    class Provider : Enumeration.Provider {
        override fun values(): Array<out Any> {
            return PrivacyProtocol.entries.toTypedArray()
        }
    }
}
