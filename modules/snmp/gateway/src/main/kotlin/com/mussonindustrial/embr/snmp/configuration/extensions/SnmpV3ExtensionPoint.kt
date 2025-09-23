package com.mussonindustrial.embr.snmp.configuration.extensions

import com.inductiveautomation.ignition.gateway.config.ExtensionPoint
import com.inductiveautomation.ignition.gateway.config.migration.ExtensionPointRecordMigrationStrategy
import com.inductiveautomation.ignition.gateway.dataroutes.openapi.SchemaUtil
import com.inductiveautomation.ignition.gateway.opcua.server.api.Device
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceContext
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceExtensionPoint
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceProfileConfig
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceSettingsRecord
import com.inductiveautomation.ignition.gateway.web.nav.ExtensionPointResourceForm
import com.inductiveautomation.ignition.gateway.web.nav.WebUiComponent
import com.mussonindustrial.embr.gateway.secrets.getAsString
import com.mussonindustrial.embr.snmp.devices.SnmpContext
import com.mussonindustrial.embr.snmp.devices.SnmpDeviceImpl
import java.util.*
import org.snmp4j.DirectUserTarget
import org.snmp4j.Snmp
import org.snmp4j.security.AuthMD5
import org.snmp4j.security.PrivAES256
import org.snmp4j.smi.Address
import org.snmp4j.smi.GenericAddress
import org.snmp4j.smi.OctetString
import org.snmp4j.transport.DefaultUdpTransportMapping

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
                    builder.withCustomFieldName(HOSTNAME, "network.hostname")
                    builder.withCustomFieldName(PORT, "network.port")
                    builder.withCustomFieldName(HEALTHCHECK_FREQUENCY, "healthcheck.frequency")
                    builder.withCustomFieldName(HEALTHCHECK_OID, "healthcheck.oid")
                    builder.withCustomFieldName(AUTH_USERNAME, "security.authentication.username")
                    builder.withCustomFieldName(AUTH_PROTOCOL, "security.authentication.protocol")
                    builder.withCustomFieldName(AUTH_PASSWORD, "security.authentication.password")
                    builder.withCustomFieldName(PRIVACY_PROTOCOL, "security.privacy.protocol")
                    builder.withCustomFieldName(PRIVACY_PASSWORD, "security.privacy.password")
                }
            }
            .build()

    override fun createDevice(
        context: DeviceContext,
        deviceConfig: DeviceProfileConfig,
        snmpConfig: Config,
    ): Device {
        val snmpContext = Context(context, deviceConfig, snmpConfig)
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

    class Config(
        override val network: SnmpNetworkConfig,
        val security: SnmpV3SecurityConfig,
        override val healthcheck: SnmpHealthcheckConfig,
    ) : SnmpDeviceConfig

    class Context(
        override val deviceContext: DeviceContext,
        override val deviceConfig: DeviceProfileConfig,
        override val snmpConfig: Config,
    ) : SnmpContext<Config> {

        val address: Address =
            GenericAddress.parse(
                ("udp:" + snmpConfig.network.hostname + "/" + snmpConfig.network.port)
            )

        val authenticationPassphrase =
            snmpConfig.security.authentication.password?.let {
                OctetString(deviceContext.gatewayContext.getAsString(it))
            }
        val privacyPassphrase =
            snmpConfig.security.privacy.password?.let {
                OctetString(deviceContext.gatewayContext.getAsString(it))
            }

        val target =
            DirectUserTarget(
                address,
                OctetString(snmpConfig.security.authentication.username),
                AuthMD5(),
                authenticationPassphrase,
                PrivAES256(),
                privacyPassphrase,
            )

        override val readTarget = target
        override val writeTarget = target

        val transportMapping = DefaultUdpTransportMapping()
        override val snmp = Snmp(transportMapping)
    }
}
