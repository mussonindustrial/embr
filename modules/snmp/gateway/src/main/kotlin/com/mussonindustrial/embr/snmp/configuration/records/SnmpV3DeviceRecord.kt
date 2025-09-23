package com.mussonindustrial.embr.snmp.configuration.records

import com.inductiveautomation.ignition.gateway.localdb.persistence.*
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceSettingsRecord
import com.mussonindustrial.embr.snmp.configuration.protocols.AuthenticationProtocol
import com.mussonindustrial.embr.snmp.configuration.protocols.PrivacyProtocol
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
        val TIMEOUT = IntField(META, "Timeout")

        val AUTH_PROTOCOL = EnumField(META, "AuthProtocol", AuthenticationProtocol::class.java)
        val AUTH_USERNAME = StringField(META, "AuthUsername")
        val AUTH_PASSWORD = EncodedStringField(META, "AuthPassword")

        val PRIVACY_PROTOCOL = EnumField(META, "PrivacyProtocol", PrivacyProtocol::class.java)
        val PRIVACY_PASSWORD = EncodedStringField(META, "PrivacyPassword")

        val HEALTHCHECK_FREQUENCY = LongField(META, "HealthcheckFrequency")
        val HEALTHCHECK_OID = StringField(META, "HealthcheckOid")
    }

    override fun getMeta(): RecordMeta<*> {
        return META
    }
}
