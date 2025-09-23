package com.mussonindustrial.embr.snmp.configuration.extensions

import com.inductiveautomation.ignition.gateway.dataroutes.openapi.annotations.*
import com.inductiveautomation.ignition.gateway.secrets.SecretConfig
import com.inductiveautomation.ignition.gateway.web.nav.FormFieldType
import com.mussonindustrial.embr.snmp.configuration.protocols.AuthenticationProtocol
import com.mussonindustrial.embr.snmp.configuration.protocols.PrivacyProtocol

interface SnmpDeviceConfig {
    val network: SnmpNetworkConfig
    val healthcheck: SnmpHealthcheckConfig
}

data class SnmpNetworkConfig(
    @FormCategoryKey("Snmp.config.category.Network")
    @Label("Hostname *")
    @DescriptionKey("Snmp.config.Network.Hostname.Description")
    @FormField(FormFieldType.TEXT)
    @Required
    val hostname: String,
    @FormCategoryKey("Snmp.config.category.Network")
    @Label("Port *")
    @DescriptionKey("Snmp.config.Network.Port.Description")
    @DefaultValue("161")
    @FormField(FormFieldType.NUMBER)
    @Required
    val port: Int,
)

data class SnmpHealthcheckConfig(
    @FormCategoryKey("Snmp.config.category.Healthcheck")
    @Label("Healthcheck Frequency *")
    @DescriptionKey("Snmp.config.Healthcheck.Frequency.Description")
    @DefaultValue("10000")
    @FormField(FormFieldType.NUMBER)
    @Required
    val frequency: Long,
    @FormCategoryKey("Snmp.config.category.Healthcheck")
    @Label("Healthcheck OID *")
    @DescriptionKey("Snmp.config.Healthcheck.Oid.Description")
    @DefaultValue("1.3.6.1.2.1.1.2.0")
    @FormField(FormFieldType.TEXT)
    @Required
    val oid: String,
)

data class SnmpCommunityConfig(
    @FormCategoryKey("Snmp.config.category.Community")
    @Label("Read Community *")
    @DescriptionKey("Snmp.config.Community.ReadCommunity.Description")
    @FormField(FormFieldType.TEXT)
    @DefaultValue("public")
    @Required
    val read: String,
    @FormCategoryKey("Snmp.config.category.Community")
    @Label("Write Community *")
    @DescriptionKey("Snmp.config.Community.WriteCommunity.Description")
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
    @FormCategoryKey("Snmp.config.category.Authentication")
    @Label("Username *")
    @DescriptionKey("Snmp.config.Authentication.Username.Description")
    @FormField(FormFieldType.TEXT)
    @Required
    val username: String,
    @FormCategoryKey("Snmp.config.category.Authentication")
    @Label("Authentication Protocol *")
    @DescriptionKey("Snmp.config.Authentication.AuthProtocol.Description")
    @FormField(FormFieldType.SELECT)
    @Enumeration(AuthenticationProtocol.Provider::class)
    @Required
    val protocol: AuthenticationProtocol,
    @FormCategoryKey("Snmp.config.category.Authentication")
    @Label("Authentication Password *")
    @DescriptionKey("Snmp.config.Authentication.AuthPassword.Description")
    @FormField(FormFieldType.SECRET)
    val password: SecretConfig?,
)

data class SnmpV3PrivacyConfig(
    @FormCategoryKey("Snmp.config.category.Authentication")
    @Label("Privacy Protocol *")
    @DescriptionKey("Snmp.config.Authentication.PrivacyProtocol.Description")
    @FormField(FormFieldType.SELECT)
    @Enumeration(PrivacyProtocol.Provider::class)
    @Required
    val protocol: PrivacyProtocol,
    @FormCategoryKey("Snmp.config.category.Authentication")
    @Label("Privacy Password *")
    @DescriptionKey("Snmp.config.Authentication.PrivacyPassword.Description")
    @FormField(FormFieldType.SECRET)
    val password: SecretConfig?,
)
