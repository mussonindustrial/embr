package com.mussonindustrial.embr.snmp.configuration.records

import com.inductiveautomation.ignition.gateway.localdb.persistence.*
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceSettingsRecord
import org.snmp4j.fluent.TargetBuilder
import simpleorm.dataset.SFieldFlags

@Deprecated("since 8.3")
@SuppressWarnings("unused")
class SnmpV3DeviceRecord : PersistentRecord() {

    @Suppress("DEPRECATION")
    companion object {
        val META = RecordMeta(SnmpV3DeviceRecord::class.java, "EmbrSnmpV3DeviceSettings")

        val DEVICE_SETTINGS_ID = LongField(META, "DeviceSettingsId", SFieldFlags.SPRIMARY_KEY)
        val DEVICE_SETTINGS =
            ReferenceField(META, DeviceSettingsRecord.META, "DeviceSettings", DEVICE_SETTINGS_ID)

        val HOSTNAME = StringField(META, "Hostname")
        val PORT = IntField(META, "Port")

        val AUTH_PROTOCOL =
            EnumField<TargetBuilder.AuthProtocol>(
                META,
                "AuthProtocol",
                TargetBuilder.AuthProtocol::class.java,
            )
        val AUTH_USERNAME = StringField(META, "AuthUsername")
        val AUTH_PASSWORD = EncodedStringField(META, "AuthPassword")
        val PRIVACY_PROTOCOL =
            EnumField(META, "PrivacyProtocol", TargetBuilder.PrivProtocol::class.java)
        val PRIVACY_PASSWORD = EncodedStringField(META, "PrivacyPassword")
        val HEALTHCHECK_FREQUENCY = LongField(META, "HealthcheckFrequency")
        val HEALTHCHECK_OID = StringField(META, "HealthcheckOid")
    }

    override fun getMeta(): RecordMeta<*> {
        return META
    }
}
