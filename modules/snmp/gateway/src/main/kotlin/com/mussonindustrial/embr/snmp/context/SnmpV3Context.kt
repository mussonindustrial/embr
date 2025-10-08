package com.mussonindustrial.embr.snmp.context

import com.inductiveautomation.ignition.common.util.LoggerEx
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceContext
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceProfileConfig
import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.mussonindustrial.embr.gateway.secrets.getAsString
import com.mussonindustrial.embr.snmp.configuration.extensions.SnmpV3ExtensionPoint.Config
import com.mussonindustrial.embr.snmp.configuration.protocols.AuthenticationProtocol
import com.mussonindustrial.embr.snmp.configuration.protocols.PrivacyProtocol
import org.snmp4j.DirectUserTarget
import org.snmp4j.Snmp
import org.snmp4j.Target
import org.snmp4j.smi.Address
import org.snmp4j.smi.GenericAddress
import org.snmp4j.smi.OctetString
import org.snmp4j.transport.DefaultTcpTransportMapping
import org.snmp4j.transport.DefaultUdpTransportMapping
import org.snmp4j.util.DefaultPDUFactory

class SnmpV3Context(
    override val deviceContext: DeviceContext,
    override val deviceConfig: DeviceProfileConfig,
    override val snmpConfig: Config,
) : SnmpContext<Config> {

    override val logger: LoggerEx =
        this.getLoggerEx(
            mapOf(
                "device-name" to deviceContext.name,
                "device-type" to deviceConfig.type,
                "address" to snmpConfig.connectivity.address,
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
            GenericAddress.parse(snmpConfig.connectivity.address)
                ?: let {
                    throw Exception("Failed to parse address ${snmpConfig.connectivity.address}.")
                }

        val authenticationPassphrase =
            snmpConfig.authentication.password
                ?.takeIf { snmpConfig.authentication.protocol != AuthenticationProtocol.None }
                ?.let { OctetString(deviceContext.gatewayContext.getAsString(it)) }

        val privacyPassphrase =
            snmpConfig.privacy.password
                ?.takeIf { snmpConfig.privacy.protocol != PrivacyProtocol.None }
                ?.let { OctetString(deviceContext.gatewayContext.getAsString(it)) }

        val target =
            DirectUserTarget(
                    address,
                    OctetString(snmpConfig.authentication.username),
                    snmpConfig.authentication.protocol.mappedProtocol,
                    authenticationPassphrase,
                    snmpConfig.privacy.protocol.mappedProtocol,
                    privacyPassphrase,
                )
                .apply { timeout = snmpConfig.connectivity.timeout.toLong() }

        readTarget = target
        writeTarget = target

        snmp.listen()
    }

    override fun shutdown() {
        snmp.close()
    }
}
