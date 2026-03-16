@file:Suppress("DEPRECATION")

package com.mussonindustrial.embr.snmp.agents.configuration.extensions

import com.inductiveautomation.ignition.gateway.config.ExtensionPoint
import com.inductiveautomation.ignition.gateway.config.ValidationErrors
import com.inductiveautomation.ignition.gateway.config.migration.ExtensionPointRecordMigrationStrategy
import com.inductiveautomation.ignition.gateway.dataroutes.openapi.SchemaUtil
import com.inductiveautomation.ignition.gateway.opcua.server.api.Device
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceContext
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceExtensionPoint
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceProfileConfig
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceSettingsRecord
import com.inductiveautomation.ignition.gateway.web.nav.ExtensionPointResourceForm
import com.inductiveautomation.ignition.gateway.web.nav.WebUiComponent
import com.mussonindustrial.embr.snmp.agents.configuration.SnmpAgentConfig
import com.mussonindustrial.embr.snmp.agents.configuration.SnmpCommunityConfig
import com.mussonindustrial.embr.snmp.agents.configuration.SnmpConnectivityConfig
import com.mussonindustrial.embr.snmp.agents.configuration.SnmpHealthcheckConfig
import com.mussonindustrial.embr.snmp.agents.configuration.records.SnmpAgentV2cDeviceRecord
import com.mussonindustrial.embr.snmp.agents.context.SnmpAgentV2cContext
import com.mussonindustrial.embr.snmp.agents.devices.SnmpAgentDeviceImpl
import java.util.*

object SnmpAgentV2cExtensionPoint :
    DeviceExtensionPoint<SnmpAgentV2cExtensionPoint.Config>(
        "embr-snmp-agent-v2c",
        "Snmp.device.SnmpAgentV2c.DisplayName",
        "Snmp.device.SnmpAgentV2c.Description",
        Config::class.java,
    ) {

    val recordMigrationStrategy: ExtensionPointRecordMigrationStrategy =
        ExtensionPointRecordMigrationStrategy.newBuilder(typeId)
            .resourceType(DEVICE_RESOURCE_TYPE)
            .profileMeta(DeviceSettingsRecord.META)
            .settingsRecordForeignKey(SnmpAgentV2cDeviceRecord.DEVICE_SETTINGS)
            .settingsMeta(SnmpAgentV2cDeviceRecord.META)
            .settingsEncoder { builder ->
                SnmpAgentV2cDeviceRecord.apply {
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
        val snmpContext = SnmpAgentV2cContext(context, deviceConfig, snmpConfig)
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
