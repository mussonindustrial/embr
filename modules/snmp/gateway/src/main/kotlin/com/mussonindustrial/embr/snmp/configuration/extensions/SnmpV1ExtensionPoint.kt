package com.mussonindustrial.embr.snmp.configuration.extensions

import com.inductiveautomation.ignition.gateway.config.ExtensionPoint
import com.inductiveautomation.ignition.gateway.config.ValidationErrors
import com.inductiveautomation.ignition.gateway.config.migration.ExtensionPointRecordMigrationStrategy
import com.inductiveautomation.ignition.gateway.dataroutes.openapi.SchemaUtil
import com.inductiveautomation.ignition.gateway.dataroutes.openapi.annotations.*
import com.inductiveautomation.ignition.gateway.opcua.server.api.*
import com.inductiveautomation.ignition.gateway.web.nav.*
import com.mussonindustrial.embr.snmp.devices.SnmpDeviceImpl
import com.mussonindustrial.embr.snmp.devices.SnmpV1Context
import java.util.*

@Suppress("DEPRECATION")
private typealias SnmpV1DeviceRecord =
    com.mussonindustrial.embr.snmp.configuration.records.SnmpV1DeviceRecord

object SnmpV1ExtensionPoint :
    DeviceExtensionPoint<SnmpV1ExtensionPoint.Config>(
        "embr-snmp-v1",
        "Snmp.device.SnmpV1Device.DisplayName",
        "Snmp.device.SnmpV1Device.Description",
        Config::class.java,
    ) {

    @Suppress("DEPRECATION")
    val recordMigrationStrategy: ExtensionPointRecordMigrationStrategy =
        ExtensionPointRecordMigrationStrategy.newBuilder(typeId)
            .resourceType(DEVICE_RESOURCE_TYPE)
            .profileMeta(DeviceSettingsRecord.META)
            .settingsRecordForeignKey(SnmpV1DeviceRecord.DEVICE_SETTINGS)
            .settingsMeta(SnmpV1DeviceRecord.META)
            .settingsEncoder { builder ->
                SnmpV1DeviceRecord.apply {
                    builder.withCustomFieldName(ADDRESS, "connectivity.address")
                    builder.withCustomFieldName(TIMEOUT, "connectivity.timeout")
                    builder.withCustomFieldName(HEALTHCHECK_FREQUENCY, "healthcheck.frequency")
                    builder.withCustomFieldName(HEALTHCHECK_OID, "healthcheck.oid")
                    builder.withCustomFieldName(COMMUNITY_READ, "community.read")
                    builder.withCustomFieldName(COMMUNITY_WRITE, "community.write")
                }
            }
            .build()

    override fun createDevice(
        context: DeviceContext,
        deviceConfig: DeviceProfileConfig,
        snmpConfig: Config,
    ): Device {
        val snmpContext = SnmpV1Context(context, deviceConfig, snmpConfig)
        return SnmpDeviceImpl(snmpContext)
    }

    override fun getWebUiComponent(type: ExtensionPoint.ComponentType): Optional<WebUiComponent> {
        return Optional.of(
            ExtensionPointResourceForm(
                DEVICE_RESOURCE_TYPE,
                "Device Connection",
                this.typeId,
                SchemaUtil.fromType(DeviceProfileConfig::class.java),
                SchemaUtil.fromType(Config::class.java),
                mutableSetOf<String>(),
            )
        )
    }

    override fun validate(settings: Config?, errors: ValidationErrors.Builder) {
        if (settings == null) {
            return
        }
        settings.connectivity.validate(errors)
        settings.community.validate(errors)
        settings.healthcheck.validate(errors)
    }

    class Config(
        override val connectivity: SnmpConnectivityConfig,
        val community: SnmpCommunityConfig,
        override val healthcheck: SnmpHealthcheckConfig,
    ) : SnmpDeviceConfig
}
