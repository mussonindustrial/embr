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
import com.mussonindustrial.embr.snmp.agents.configuration.SnmpConnectivityConfig
import com.mussonindustrial.embr.snmp.agents.configuration.SnmpHealthcheckConfig
import com.mussonindustrial.embr.snmp.agents.configuration.SnmpV3AuthenticationConfig
import com.mussonindustrial.embr.snmp.agents.configuration.SnmpV3PrivacyConfig
import com.mussonindustrial.embr.snmp.agents.configuration.records.SnmpAgentV3DeviceRecord
import com.mussonindustrial.embr.snmp.agents.context.SnmpAgentV3Context
import com.mussonindustrial.embr.snmp.agents.devices.SnmpAgentDeviceImpl
import java.util.*

object SnmpAgentV3ExtensionPoint :
    DeviceExtensionPoint<SnmpAgentV3ExtensionPoint.Config>(
        "embr-snmp-agent-v3",
        "Snmp.device.SnmpAgentV3.DisplayName",
        "Snmp.device.SnmpAgentV3.Description",
        Config::class.java,
    ) {

    val recordMigrationStrategy: ExtensionPointRecordMigrationStrategy =
        ExtensionPointRecordMigrationStrategy.newBuilder(typeId)
            .resourceType(DEVICE_RESOURCE_TYPE)
            .profileMeta(DeviceSettingsRecord.META)
            .settingsRecordForeignKey(SnmpAgentV3DeviceRecord.DEVICE_SETTINGS)
            .settingsMeta(SnmpAgentV3DeviceRecord.META)
            .settingsEncoder { builder ->
                SnmpAgentV3DeviceRecord.apply {
                    builder.withCustomFieldName(ADDRESS, "connectivity.address")
                    builder.withCustomFieldName(TIMEOUT, "connectivity.timeout")
                    builder.withCustomFieldName(HEALTHCHECK_FREQUENCY, "healthcheck.frequency")
                    builder.withCustomFieldName(HEALTHCHECK_OID, "healthcheck.oid")
                    builder.withCustomFieldName(AUTH_USERNAME, "authentication.username")
                    builder.withCustomFieldName(AUTH_PROTOCOL, "authentication.protocol")
                    builder.withCustomFieldName(AUTH_PASSWORD, "authentication.password")
                    builder.withCustomFieldName(PRIVACY_PROTOCOL, "privacy.protocol")
                    builder.withCustomFieldName(PRIVACY_PASSWORD, "privacy.password")
                }
            }
            .build()

    override fun createDevice(
        context: DeviceContext,
        deviceConfig: DeviceProfileConfig,
        snmpConfig: Config,
    ): Device {
        val snmpContext = SnmpAgentV3Context(context, deviceConfig, snmpConfig)
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
        settings.healthcheck.validate(errors)
    }

    class Config(
        override val connectivity: SnmpConnectivityConfig,
        val authentication: SnmpV3AuthenticationConfig,
        val privacy: SnmpV3PrivacyConfig,
        override val healthcheck: SnmpHealthcheckConfig,
    ) : SnmpAgentConfig
}
