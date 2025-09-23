package com.mussonindustrial.embr.snmp.configuration.records

import com.inductiveautomation.ignition.gateway.localdb.persistence.*
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceSettingsRecord
import com.inductiveautomation.ignition.gateway.web.components.editors.PasswordEditorSource
import com.mussonindustrial.embr.snmp.configuration.settings.SnmpV3DeviceSettings
import org.snmp4j.fluent.TargetBuilder
import simpleorm.dataset.SFieldFlags

class SnmpV3DeviceRecord : SnmpV3DeviceSettings, PersistentRecord() {

    companion object {
        val META = RecordMeta(SnmpV3DeviceRecord::class.java, "EmbrSnmpV3DeviceSettings")

        val DEVICE_SETTINGS_ID = LongField(META, "DeviceSettingsId", SFieldFlags.SPRIMARY_KEY)
        val DEVICE_SETTINGS =
            ReferenceField(META, DeviceSettingsRecord.META, "DeviceSettings", DEVICE_SETTINGS_ID)
                .apply { formMeta.isVisible = false }

        val HOSTNAME = StringField(META, "Hostname", SFieldFlags.SMANDATORY)
        val PORT = IntField(META, "Port", SFieldFlags.SMANDATORY).apply { default = 161 }
        val CATEGORY_NETWORK =
            Category("SnmpV3DeviceRecord.Network", 1001).apply {
                include(HOSTNAME)
                include(PORT)
            }

        val AUTH_PROTOCOL =
            EnumField<TargetBuilder.AuthProtocol>(
                META,
                "AuthProtocol",
                TargetBuilder.AuthProtocol::class.java,
            )
        val AUTH_USERNAME = StringField(META, "AuthUsername")
        val AUTH_PASSWORD =
            EncodedStringField(META, "AuthPassword").apply {
                formMeta.editorSource = PasswordEditorSource.getSharedInstance()
            }
        val CATEGORY_AUTH =
            Category("SnmpV3DeviceRecord.Auth", 1002).apply {
                include(AUTH_PROTOCOL)
                include(AUTH_USERNAME)
                include(AUTH_PASSWORD)
            }

        val PRIVACY_PROTOCOL = EnumField(META, "PrivacyProtocol", PrivacyProtocol::class.java)
        val PRIVACY_PASSWORD =
            EncodedStringField(META, "PrivacyPassword").apply {
                formMeta.editorSource = PasswordEditorSource.getSharedInstance()
            }
        val CATEGORY_PRIVACY =
            Category("SnmpV3DeviceRecord.Privacy", 1003).apply {
                include(PRIVACY_PROTOCOL)
                include(PRIVACY_PASSWORD)
            }

        val HEALTHCHECK_FREQUENCY =
            LongField(META, "HealthcheckFrequency", SFieldFlags.SMANDATORY).apply {
                default = 10000
            }
        val HEALTHCHECK_OID =
            StringField(META, "HealthcheckOid", SFieldFlags.SMANDATORY).apply {
                default = "1.3.6.1.2.1.1.2.0"
            }
        val CATEGORY_HEALTHCHECK =
            Category("SnmpV3DeviceRecord.Healthcheck", 1004, true).apply {
                include(HEALTHCHECK_FREQUENCY)
                include(HEALTHCHECK_OID)
            }
    }

    override fun getMeta(): RecordMeta<*> {
        return META
    }

    override val hostname: String
        get() = getString(HOSTNAME)

    override val port: Int
        get() = getInt(PORT)

    override val healthcheckFrequency: Long
        get() = getLong(HEALTHCHECK_FREQUENCY)

    override val healthcheckOid: String
        get() = getString(HEALTHCHECK_OID)

    override val username: String
        get() = getString(AUTH_USERNAME)

    override val authProtocol: TargetBuilder.AuthProtocol
        get() = getEnum(AUTH_PROTOCOL)

    override val authPassword: String
        get() = getString(AUTH_PASSWORD)

    override val privacyProtocol: TargetBuilder.PrivProtocol
        get() = getEnum(PRIVACY_PROTOCOL)

    override val privacyPassword: String
        get() = getString(PRIVACY_PASSWORD)
}
