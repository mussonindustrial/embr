package com.mussonindustrial.embr.snmp.configuration.extensions

import com.inductiveautomation.ignition.gateway.config.ExtensionPoint
import com.inductiveautomation.ignition.gateway.config.ValidationErrors
import com.inductiveautomation.ignition.gateway.config.migration.ExtensionPointRecordMigrationStrategy
import com.inductiveautomation.ignition.gateway.dataroutes.openapi.SchemaUtil
import com.inductiveautomation.ignition.gateway.dataroutes.openapi.annotations.*
import com.inductiveautomation.ignition.gateway.opcua.server.api.*
import com.inductiveautomation.ignition.gateway.web.nav.*
import com.mussonindustrial.embr.snmp.devices.SnmpContext
import com.mussonindustrial.embr.snmp.devices.SnmpDeviceImpl
import java.util.*
import org.snmp4j.CommunityTarget
import org.snmp4j.Snmp
import org.snmp4j.mp.SnmpConstants
import org.snmp4j.smi.Address
import org.snmp4j.smi.GenericAddress
import org.snmp4j.smi.OctetString
import org.snmp4j.transport.DefaultUdpTransportMapping

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
                    builder.withCustomFieldName(HOSTNAME, "connectivity.hostname")
                    builder.withCustomFieldName(PORT, "connectivity.port")
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

    class Context(
        override val deviceContext: DeviceContext,
        override val deviceConfig: DeviceProfileConfig,
        override val snmpConfig: Config,
    ) : SnmpContext<Config> {

        val address: Address =
            GenericAddress.parse(
                ("udp:" + snmpConfig.connectivity.hostname + "/" + snmpConfig.connectivity.port)
            )

        override val readTarget =
            CommunityTarget(address, OctetString(snmpConfig.community.read)).apply {
                version = SnmpConstants.version1
                timeout = snmpConfig.connectivity.timeout
            }
        override val writeTarget =
            snmpConfig.community.write?.let {
                CommunityTarget(address, OctetString(it)).apply {
                    version = SnmpConstants.version1
                    timeout = snmpConfig.connectivity.timeout
                }
            }

        val transportMapping = DefaultUdpTransportMapping()
        override val snmp = Snmp(transportMapping)
    }
}
