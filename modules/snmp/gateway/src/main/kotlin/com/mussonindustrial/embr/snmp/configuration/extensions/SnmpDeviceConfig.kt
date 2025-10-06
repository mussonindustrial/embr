package com.mussonindustrial.embr.snmp.configuration.extensions

import com.inductiveautomation.ignition.gateway.config.ValidationErrors
import com.inductiveautomation.ignition.gateway.dataroutes.openapi.annotations.*
import com.inductiveautomation.ignition.gateway.secrets.SecretConfig
import com.inductiveautomation.ignition.gateway.web.nav.FormFieldType
import com.mussonindustrial.embr.snmp.configuration.protocols.AuthenticationProtocol
import com.mussonindustrial.embr.snmp.configuration.protocols.PrivacyProtocol
import org.snmp4j.smi.GenericAddress
import org.snmp4j.smi.OID

interface SnmpDeviceConfig {
    val connectivity: SnmpConnectivityConfig
    val healthcheck: SnmpHealthcheckConfig
}

data class SnmpConnectivityConfig(
    @FormCategoryKey("Snmp.config.category.Connectivity")
    @Label("Address *")
    @DescriptionKey("Snmp.config.Connectivity.Address.Description")
    @ExampleValue("udp:127.0.0.1/161")
    @FormField(FormFieldType.TEXT)
    @Required
    val address: String,
    @FormCategoryKey("Snmp.config.category.Connectivity")
    @Label("Timeout *")
    @DescriptionKey("Snmp.config.Connectivity.Timeout.Description")
    @DefaultValue("1000")
    @FormField(FormFieldType.NUMBER)
    @Required
    val timeout: Int,
) {
    fun validate(errors: ValidationErrors.Builder) =
        errors.apply {
            requireNotBlank("connectivity.address", address)
            checkField(
                GenericAddress.parse(address) != null,
                "connectivity.address",
                "Address must be valid.",
            )
            checkField(timeout >= 1, "connectivity.timeout", "Timeout must not be negative")
        }
}

data class SnmpHealthcheckConfig(
    @FormCategoryKey("Snmp.config.category.Healthcheck")
    @Label("Healthcheck Frequency")
    @DescriptionKey("Snmp.config.Healthcheck.Frequency.Description")
    @DefaultValue("10000")
    @FormField(FormFieldType.NUMBER)
    val frequency: Int?,
    @FormCategoryKey("Snmp.config.category.Healthcheck")
    @Label("Healthcheck OID")
    @DescriptionKey("Snmp.config.Healthcheck.Oid.Description")
    @DefaultValue("1.3.6.1.2.1.1.2.0")
    @FormField(FormFieldType.TEXT)
    val oid: String?,
) {
    fun validate(errors: ValidationErrors.Builder) =
        errors.apply {
            frequency?.let {
                checkField(
                    frequency >= 0,
                    "healthcheck.frequency",
                    "Frequency must not be negative",
                )
            }

            oid?.let {
                try {
                    OID(oid)
                } catch (_: Exception) {
                    checkField(false, "healthcheck.oid", "Healthcheck OID must be a valid OID")
                }
            }
        }
}

data class SnmpCommunityConfig(
    @FormCategoryKey("Snmp.config.category.Community")
    @Label("Read Community *")
    @DescriptionKey("Snmp.config.Community.ReadCommunity.Description")
    @FormField(FormFieldType.TEXT)
    @DefaultValue("public")
    @Required
    val read: String,
    @FormCategoryKey("Snmp.config.category.Community")
    @Label("Write Community")
    @DescriptionKey("Snmp.config.Community.WriteCommunity.Description")
    @FormField(FormFieldType.TEXT)
    @DefaultValue("public")
    val write: String?,
) {
    fun validate(errors: ValidationErrors.Builder) =
        errors.apply { requireNotBlank("community.read", read) }
}

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
    @FormChoices(
        ids = ["None", "Md5", "Sha1", "Sha224", "Sha256", "Sha384", "Sha512"],
        labels = ["None", "MD-5", "SHA-1", "SHA-224", "SHA-256", "SHA-384", "SHA-512"],
    )
    @Required
    val protocol: AuthenticationProtocol,
    @FormCategoryKey("Snmp.config.category.Authentication")
    @Label("Authentication Password")
    @DescriptionKey("Snmp.config.Authentication.AuthPassword.Description")
    @FormField(FormFieldType.SECRET)
    val password: SecretConfig?,
)

data class SnmpV3PrivacyConfig(
    @FormCategoryKey("Snmp.config.category.Authentication")
    @Label("Privacy Protocol *")
    @DescriptionKey("Snmp.config.Authentication.PrivacyProtocol.Description")
    @FormField(FormFieldType.SELECT)
    @FormChoices(
        ids = ["None", "Des", "Tdes", "Aes128", "Aes192", "Aes256", "Aes192Tdes", "Aes256Tdes"],
        labels =
            [
                "None",
                "DES",
                "3DES/TDES/TDEA",
                "AES-128",
                "AES-192",
                "AES-256",
                "AES-192 + 3DES/TDES/TDEA Key Extension",
                "AES-256 + 3DES/TDES/TDEA Key Extension",
            ],
    )
    @Required
    val protocol: PrivacyProtocol,
    @FormCategoryKey("Snmp.config.category.Authentication")
    @Label("Privacy Password")
    @DescriptionKey("Snmp.config.Authentication.PrivacyPassword.Description")
    @FormField(FormFieldType.SECRET)
    val password: SecretConfig?,
)
