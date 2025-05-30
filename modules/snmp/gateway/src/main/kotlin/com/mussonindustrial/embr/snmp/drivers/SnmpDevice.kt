package com.mussonindustrial.embr.snmp.drivers

import com.inductiveautomation.ignition.gateway.opcua.server.api.*
import com.mussonindustrial.embr.common.logging.getLogger
import com.mussonindustrial.embr.snmp.configuration.SnmpDeviceSettings
import com.mussonindustrial.embr.snmp.utils.addOID
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import org.eclipse.milo.opcua.sdk.server.api.AddressSpaceComposite
import org.snmp4j.CommunityTarget
import org.snmp4j.PDU
import org.snmp4j.Snmp
import org.snmp4j.Target
import org.snmp4j.smi.*
import org.snmp4j.transport.DefaultUdpTransportMapping

class SnmpDevice(
    val deviceContext: DeviceContext,
    private val deviceSettings: DeviceSettingsRecord,
    private val snmpSettings: SnmpDeviceSettings,
) : AddressSpaceComposite(deviceContext.getServer()), Device {

    private val logger = this.getLogger()

    private var status: Status = Status.DISCONNECTED
    private var healthcheck: ScheduledFuture<*>? = null

    private lateinit var target: Target<Address>
    private lateinit var snmp: Snmp

    private lateinit var browseAddressSpace: BrowsableAddressSpace
    private lateinit var oidAddressSpace: OidAddressSpace

    override fun getName(): String {
        return deviceSettings.name
    }

    override fun getStatus(): String {
        return status.toString()
    }

    override fun getTypeId(): String {
        return deviceSettings.type
    }

    override fun startup() {
        logger.debug("Starting up...")

        val address: Address =
            GenericAddress.parse(("udp:" + snmpSettings.hostname + "/" + snmpSettings.port))
        val community = OctetString(snmpSettings.community)
        target = CommunityTarget(address, community)

        val transportMapping = DefaultUdpTransportMapping()
        snmp = Snmp(transportMapping)
        snmp.listen()

        //        browseAddressSpace = BrowsableAddressSpace(deviceContext.getServer(), this)
        //        browseAddressSpace.startup()

        oidAddressSpace = OidAddressSpace(this, snmp, target)
        oidAddressSpace.startup()

        // fire initial subscription creation
        onDataItemsCreated(deviceContext.getSubscriptionModel().getDataItems(getName()))

        startHealthcheck()
    }

    override fun shutdown() {
        logger.debug("Shutting down...")
        snmp.close()

        //        browseAddressSpace.shutdown()
        oidAddressSpace.shutdown()

        stopHealthcheck()
    }

    private fun startHealthcheck() {
        healthcheck?.cancel(true)

        healthcheck =
            deviceContext
                .getGatewayContext()
                .scheduledExecutorService
                .scheduleWithFixedDelay(
                    this::doHealthcheck,
                    1000,
                    snmpSettings.connectionTimeout,
                    TimeUnit.MILLISECONDS,
                )
    }

    private fun stopHealthcheck() {
        healthcheck?.cancel(true)
    }

    private fun doHealthcheck() {
        if (snmpSettings.healthcheckOid.isBlank()) {
            return
        }

        val response =
            snmp.send(
                PDU().apply {
                    type = PDU.GET
                    addOID(snmpSettings.healthcheckOid)
                },
                target,
            )
        if (response == null) {
            this.status = Status.DISCONNECTED
        } else {
            this.status = Status.CONNECTED
        }
    }

    enum class Status(private val value: String) {
        DISCONNECTED("Disconnected"),
        CONNECTED("Connected");

        override fun toString(): String {
            return value
        }
    }
}
