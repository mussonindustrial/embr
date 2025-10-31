package com.mussonindustrial.embr.snmp.agents.context

import com.inductiveautomation.ignition.common.util.LoggerEx
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceContext
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceSettingsRecord
import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.mussonindustrial.embr.snmp.agents.configuration.settings.SnmpAgentV1DeviceSettings
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

class SnmpAgentV1Context(
    override val deviceContext: DeviceContext,
    override val deviceSettings: DeviceSettingsRecord,
    override val snmpSettings: SnmpAgentV1DeviceSettings,
) : SnmpAgentContext<SnmpAgentV1DeviceSettings> {

    override val logger: LoggerEx =
        this.getLoggerEx(
            mapOf(
                "device-name" to this.deviceContext.getName(),
                "device-type" to this.deviceSettings.type,
                "address" to this.snmpSettings.address,
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
            GenericAddress.parse(snmpSettings.address)
                ?: let { throw Exception("Failed to parse address ${snmpSettings.address}.") }

        readTarget =
            CommunityTarget(address, OctetString(snmpSettings.communityRead)).apply {
                version = SnmpConstants.version1
                timeout = snmpSettings.timeout.toLong()
            }
        writeTarget =
            snmpSettings.communityWrite?.let {
                CommunityTarget(address, OctetString(snmpSettings.communityWrite)).apply {
                    version = SnmpConstants.version1
                    timeout = snmpSettings.timeout.toLong()
                }
            }

        snmp.listen()
    }

    override fun shutdown() {
        snmp.close()
    }
}
