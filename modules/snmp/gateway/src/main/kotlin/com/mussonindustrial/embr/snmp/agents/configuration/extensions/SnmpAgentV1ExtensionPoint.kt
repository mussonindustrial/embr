@file:Suppress("DEPRECATION")

package com.mussonindustrial.embr.snmp.agents.configuration.extensions

import com.inductiveautomation.ignition.gateway.config.ExtensionPoint
import com.inductiveautomation.ignition.gateway.config.ValidationErrors
import com.inductiveautomation.ignition.gateway.dataroutes.openapi.SchemaUtil
import com.inductiveautomation.ignition.gateway.opcua.server.api.*
import com.inductiveautomation.ignition.gateway.web.nav.*
import com.mussonindustrial.embr.snmp.agents.configuration.SnmpAgentConfig
import com.mussonindustrial.embr.snmp.agents.configuration.SnmpCommunityConfig
import com.mussonindustrial.embr.snmp.agents.configuration.SnmpConnectivityConfig
import com.mussonindustrial.embr.snmp.agents.configuration.SnmpHealthcheckConfig
import com.mussonindustrial.embr.snmp.agents.context.SnmpAgentV1Context
import com.mussonindustrial.embr.snmp.agents.devices.SnmpAgentDeviceImpl
import java.util.*

object SnmpAgentV1ExtensionPoint :
    DeviceExtensionPoint<SnmpAgentV1ExtensionPoint.Config>(
        "embr-snmp-agent-v1",
        "Snmp.device.SnmpAgentV1.DisplayName",
        "Snmp.device.SnmpAgentV1.Description",
        Config::class.java,
    ) {

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
            ),
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
