package com.mussonindustrial.embr.snmp.configuration.extensions

import com.inductiveautomation.ignition.gateway.dataroutes.openapi.annotations.DefaultValue
import com.inductiveautomation.ignition.gateway.dataroutes.openapi.annotations.Description
import com.inductiveautomation.ignition.gateway.dataroutes.openapi.annotations.Enumeration
import com.inductiveautomation.ignition.gateway.dataroutes.openapi.annotations.FormCategory
import com.inductiveautomation.ignition.gateway.dataroutes.openapi.annotations.FormField
import com.inductiveautomation.ignition.gateway.dataroutes.openapi.annotations.Label
import com.inductiveautomation.ignition.gateway.dataroutes.openapi.annotations.Required
import com.inductiveautomation.ignition.gateway.secrets.SecretConfig
import com.inductiveautomation.ignition.gateway.web.nav.FormFieldType
import com.mussonindustrial.embr.snmp.configuration.extensions.SnmpV3ExtensionPoint.AuthenticationProtocolProvider
import com.mussonindustrial.embr.snmp.configuration.extensions.SnmpV3ExtensionPoint.PrivacyProtocolProvider
import org.snmp4j.fluent.TargetBuilder

interface SnmpDeviceConfig {
    val network: SnmpNetworkConfig
    val healthcheck: SnmpHealthcheckConfig
}

data class SnmpNetworkConfig(
    @FormCategory("NETWORK")
    @Label("Hostname *")
    @Description("Hostname/IP address of the SNMP device.")
    @FormField(FormFieldType.TEXT)
    @Required
    val hostname: String,
    @FormCategory("NETWORK")
    @Label("Port *")
    @Description("Port to connect to on the remote device.")
    @DefaultValue("161")
    @FormField(FormFieldType.NUMBER)
    @Required
    val port: Int,
)

data class SnmpHealthcheckConfig(
    @FormCategory("HEALTHCHECK")
    @Label("Healthcheck Frequency *")
    @Description("The time in milliseconds between connection validation checks.")
    @DefaultValue("10000")
    @FormField(FormFieldType.NUMBER)
    @Required
    val frequency: Long,
    @FormCategory("HEALTHCHECK")
    @Label("Healthcheck OID *")
    @Description("This OID will be queried to validate the connection.")
    @DefaultValue("1.3.6.1.2.1.1.2.0")
    @FormField(FormFieldType.TEXT)
    @Required
    val oid: String,
)

data class SnmpCommunityConfig(
    @FormCategory("COMMUNITY")
    @Label("Read Community *")
    @Description("The SNMP community used for reads.")
    @FormField(FormFieldType.TEXT)
    @DefaultValue("public")
    @Required
    val read: String,
    @FormCategory("COMMUNITY")
    @Label("Write Community *")
    @Description("The SNMP community used for writes.")
    @FormField(FormFieldType.TEXT)
    @DefaultValue("public")
    @Required
    val write: String,
)

data class SnmpV3SecurityConfig(
    val authentication: SnmpV3AuthenticationConfig,
    val privacy: SnmpV3PrivacyConfig,
)

data class SnmpV3AuthenticationConfig(
    @FormCategory("AUTHENTICATION")
    @Label("Username *")
    @Description("Username")
    @FormField(FormFieldType.TEXT)
    @Required
    val username: String,
    @FormCategory("AUTHENTICATION")
    @Label("Authentication Protocol *")
    @Description("Protocol used for authentication")
    @FormField(FormFieldType.SELECT)
    @Enumeration(AuthenticationProtocolProvider::class)
    @Required
    val protocol: TargetBuilder.AuthProtocol,
    @FormCategory("AUTHENTICATION")
    @Label("Authentication Password *")
    @Description("Password used for authentication")
    @FormField(FormFieldType.SECRET)
    @Required
    val password: SecretConfig,
)

data class SnmpV3PrivacyConfig(
    @FormCategory("AUTHENTICATION")
    @Label("Privacy Protocol *")
    @Description("Protocol used for privacy")
    @FormField(FormFieldType.SELECT)
    @Enumeration(PrivacyProtocolProvider::class)
    @Required
    val protocol: TargetBuilder.PrivProtocol,
    @FormCategory("AUTHENTICATION")
    @Label("Privacy Password *")
    @Description("Password used for privacy")
    @FormField(FormFieldType.SECRET)
    @Required
    val password: SecretConfig,
)
