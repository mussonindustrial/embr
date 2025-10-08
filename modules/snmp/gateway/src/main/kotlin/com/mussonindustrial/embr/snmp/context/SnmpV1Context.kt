package com.mussonindustrial.embr.snmp.context

import com.inductiveautomation.ignition.common.util.LoggerEx
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceContext
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceProfileConfig
import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.mussonindustrial.embr.snmp.configuration.extensions.SnmpV1ExtensionPoint.Config
import org.snmp4j.CommunityTarget
import org.snmp4j.Snmp
import org.snmp4j.Target
import org.snmp4j.mp.SnmpConstants
import org.snmp4j.smi.Address
import org.snmp4j.smi.GenericAddress
import org.snmp4j.smi.OctetString
import org.snmp4j.transport.DefaultTcpTransportMapping
import org.snmp4j.transport.DefaultUdpTransportMapping
import org.snmp4j.util.DefaultPDUFactory

class SnmpV1Context(
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
    override var writeTarget: Target<Address>? = null

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

        readTarget =
            CommunityTarget(address, OctetString(snmpConfig.community.read)).apply {
                version = SnmpConstants.version1
                timeout = snmpConfig.connectivity.timeout.toLong()
            }
        writeTarget =
            snmpConfig.community.write?.let {
                CommunityTarget(address, OctetString(snmpConfig.community.write)).apply {
                    version = SnmpConstants.version1
                    timeout = snmpConfig.connectivity.timeout.toLong()
                }
            }

        snmp.listen()
    }

    override fun shutdown() {
        snmp.close()
    }
}
