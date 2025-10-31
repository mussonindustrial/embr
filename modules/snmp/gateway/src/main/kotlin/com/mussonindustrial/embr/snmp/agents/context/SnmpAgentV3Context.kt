package com.mussonindustrial.embr.snmp.agents.context

import com.inductiveautomation.ignition.common.util.LoggerEx
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceContext
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceSettingsRecord
import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.mussonindustrial.embr.snmp.agents.configuration.settings.SnmpAgentV3DeviceSettings
import com.mussonindustrial.embr.snmp.protocols.AuthenticationProtocol
import com.mussonindustrial.embr.snmp.protocols.PrivacyProtocol
import org.snmp4j.DirectUserTarget
import org.snmp4j.Snmp
import org.snmp4j.Target
import org.snmp4j.smi.Address
import org.snmp4j.smi.GenericAddress
import org.snmp4j.smi.OctetString
import org.snmp4j.transport.DefaultTcpTransportMapping
import org.snmp4j.transport.DefaultUdpTransportMapping
import org.snmp4j.util.DefaultPDUFactory

class SnmpAgentV3Context(
    override val deviceContext: DeviceContext,
    override val deviceSettings: DeviceSettingsRecord,
    override val snmpSettings: SnmpAgentV3DeviceSettings,
) : SnmpAgentContext<SnmpAgentV3DeviceSettings> {

    override val logger: LoggerEx =
        this.getLoggerEx(
            mapOf(
                "device-name" to this.deviceContext.getName(),
                "device-type" to this.deviceSettings.type,
                "address" to this.snmpSettings.address,
            )
        )

    override lateinit var readTarget: Target<Address>
    override lateinit var writeTarget: Target<Address>

    override val pduFactory = DefaultPDUFactory()
    override val snmp =
        Snmp(DefaultUdpTransportMapping()).apply {
            addTransportMapping(DefaultTcpTransportMapping())
        }

    override fun startup() {
        val address: Address =
            GenericAddress.parse(snmpSettings.address)
                ?: let { throw Exception("Failed to parse address ${snmpSettings.address}.") }

        val authenticationPassphrase =
            snmpSettings.authPassword
                ?.takeIf { snmpSettings.authProtocol != AuthenticationProtocol.None }
                ?.let { OctetString(it) }

        val privacyPassphrase =
            snmpSettings.privacyPassword
                ?.takeIf { snmpSettings.privacyProtocol != PrivacyProtocol.None }
                ?.let { OctetString(it) }

        val target =
            DirectUserTarget(
                    address,
                    OctetString(snmpSettings.username),
                    snmpSettings.authProtocol.mappedProtocol,
                    authenticationPassphrase,
                    snmpSettings.privacyProtocol.mappedProtocol,
                    privacyPassphrase,
                )
                .apply { timeout = snmpSettings.timeout.toLong() }

        readTarget = target
        writeTarget = target

        snmp.listen()
    }

    override fun shutdown() {
        snmp.close()
    }
}
