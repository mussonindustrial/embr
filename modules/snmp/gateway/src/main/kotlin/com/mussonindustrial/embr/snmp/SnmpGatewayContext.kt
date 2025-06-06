package com.mussonindustrial.embr.snmp

import com.inductiveautomation.ignition.gateway.model.DiagnosticsManager
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.inductiveautomation.ignition.gateway.model.TelemetryManager
import com.mussonindustrial.embr.gateway.EmbrGatewayContext
import com.mussonindustrial.embr.gateway.EmbrGatewayContextImpl
import com.mussonindustrial.embr.snmp.configuration.SnmpV1DeviceRecord
import com.mussonindustrial.embr.snmp.configuration.SnmpV1DeviceType
import com.mussonindustrial.embr.snmp.configuration.SnmpV2CDeviceRecord
import com.mussonindustrial.embr.snmp.configuration.SnmpV2CDeviceType
import com.mussonindustrial.embr.snmp.configuration.SnmpV3DeviceRecord
import com.mussonindustrial.embr.snmp.configuration.SnmpV3DeviceType

class SnmpGatewayContext(private val context: GatewayContext) :
    EmbrGatewayContext by EmbrGatewayContextImpl(context) {
    companion object {
        lateinit var instance: SnmpGatewayContext
    }

    val deviceTypes = listOf(SnmpV1DeviceType, SnmpV2CDeviceType, SnmpV3DeviceType)
    private val records =
        listOf(SnmpV1DeviceRecord.META, SnmpV2CDeviceRecord.META, SnmpV3DeviceRecord.META)

    init {
        instance = this
    }

    fun updatePersistentRecords() {
        schemaUpdater.updatePersistentRecords(records)
    }

    override fun getTelemetryManager(): TelemetryManager? {
        return super.getTelemetryManager()
    }

    override fun getDiagnosticsManager(): DiagnosticsManager? {
        return super.getDiagnosticsManager()
    }
}
