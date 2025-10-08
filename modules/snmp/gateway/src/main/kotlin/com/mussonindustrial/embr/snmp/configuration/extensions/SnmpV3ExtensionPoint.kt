package com.mussonindustrial.embr.snmp.configuration.extensions

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
import com.mussonindustrial.embr.snmp.context.SnmpV3Context
import com.mussonindustrial.embr.snmp.devices.SnmpDeviceImpl
import java.util.*

@Suppress("DEPRECATION")
private typealias SnmpV3DeviceRecord =
    com.mussonindustrial.embr.snmp.configuration.records.SnmpV3DeviceRecord

object SnmpV3ExtensionPoint :
    DeviceExtensionPoint<SnmpV3ExtensionPoint.Config>(
        "embr-snmp-v3",
        "Snmp.device.SnmpV3Device.DisplayName",
        "Snmp.device.SnmpV3Device.Description",
        Config::class.java,
    ) {

    @Suppress("DEPRECATION")
    val recordMigrationStrategy: ExtensionPointRecordMigrationStrategy =
        ExtensionPointRecordMigrationStrategy.newBuilder(typeId)
            .resourceType(DEVICE_RESOURCE_TYPE)
            .profileMeta(DeviceSettingsRecord.META)
            .settingsRecordForeignKey(SnmpV3DeviceRecord.DEVICE_SETTINGS)
            .settingsMeta(SnmpV3DeviceRecord.META)
            .settingsEncoder { builder ->
                SnmpV3DeviceRecord.apply {
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
        val snmpContext = SnmpV3Context(context, deviceConfig, snmpConfig)
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
        settings.healthcheck.validate(errors)
    }

    class Config(
        override val connectivity: SnmpConnectivityConfig,
        val authentication: SnmpV3AuthenticationConfig,
        val privacy: SnmpV3PrivacyConfig,
        override val healthcheck: SnmpHealthcheckConfig,
    ) : SnmpDeviceConfig
}
