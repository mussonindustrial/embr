package com.mussonindustrial.embr.snmp.agents.configuration.extensions

import com.inductiveautomation.ignition.gateway.config.ExtensionPoint
import com.inductiveautomation.ignition.gateway.config.ValidationErrors
import com.inductiveautomation.ignition.gateway.config.migration.ExtensionPointRecordMigrationStrategy
import com.inductiveautomation.ignition.gateway.dataroutes.openapi.SchemaUtil
import com.inductiveautomation.ignition.gateway.dataroutes.openapi.annotations.*
import com.inductiveautomation.ignition.gateway.opcua.server.api.*
import com.inductiveautomation.ignition.gateway.web.nav.*
import com.mussonindustrial.embr.snmp.agents.configuration.SnmpAgentConfig
import com.mussonindustrial.embr.snmp.agents.configuration.SnmpCommunityConfig
import com.mussonindustrial.embr.snmp.agents.configuration.SnmpConnectivityConfig
import com.mussonindustrial.embr.snmp.agents.configuration.SnmpHealthcheckConfig
import com.mussonindustrial.embr.snmp.agents.context.SnmpAgentV1Context
import com.mussonindustrial.embr.snmp.agents.devices.SnmpAgentDeviceImpl
import java.util.*

@Suppress("DEPRECATION")
private typealias SnmpAgentV1DeviceRecord =
    com.mussonindustrial.embr.snmp.agents.configuration.records.SnmpAgentV1DeviceRecord

object SnmpAgentV1ExtensionPoint :
    DeviceExtensionPoint<SnmpAgentV1ExtensionPoint.Config>(
        "embr-snmp-agent-v1",
        "Snmp.device.SnmpAgentV1.DisplayName",
        "Snmp.device.SnmpAgentV1.Description",
        Config::class.java,
    ) {

    @Suppress("DEPRECATION")
    val recordMigrationStrategy: ExtensionPointRecordMigrationStrategy =
        ExtensionPointRecordMigrationStrategy.newBuilder(typeId)
            .resourceType(DEVICE_RESOURCE_TYPE)
            .profileMeta(DeviceSettingsRecord.META)
            .settingsRecordForeignKey(SnmpAgentV1DeviceRecord.DEVICE_SETTINGS)
            .settingsMeta(SnmpAgentV1DeviceRecord.META)
            .settingsEncoder { builder ->
                SnmpAgentV1DeviceRecord.apply {
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
        val snmpContext = SnmpAgentV1Context(context, deviceConfig, snmpConfig)
        return SnmpAgentDeviceImpl(snmpContext)
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
    ) : SnmpAgentConfig
}
