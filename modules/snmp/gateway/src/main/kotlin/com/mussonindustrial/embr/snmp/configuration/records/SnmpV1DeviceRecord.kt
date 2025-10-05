package com.mussonindustrial.embr.snmp.configuration.records

import com.inductiveautomation.ignition.gateway.localdb.persistence.*
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceSettingsRecord
import simpleorm.dataset.SFieldFlags

@Deprecated("since 8.3")
@SuppressWarnings("unused")
class SnmpV1DeviceRecord : PersistentRecord() {

    @Suppress("DEPRECATION")
    companion object {
        val META = RecordMeta(SnmpV1DeviceRecord::class.java, "EmbrSnmpV1DeviceSettings")

        val DEVICE_SETTINGS_ID = LongField(META, "DeviceSettingsId", SFieldFlags.SPRIMARY_KEY)
        val DEVICE_SETTINGS =
            ReferenceField(META, DeviceSettingsRecord.META, "DeviceSettings", DEVICE_SETTINGS_ID)

        val ADDRESS = StringField(META, "Address")
        val PORT = IntField(META, "Port")
        val TIMEOUT = IntField(META, "Timeout")

        val COMMUNITY_READ = StringField(META, "CommunityRead")
        val COMMUNITY_WRITE = StringField(META, "CommunityWrite")

        val HEALTHCHECK_FREQUENCY = IntField(META, "HealthcheckFrequency")
        val HEALTHCHECK_OID = StringField(META, "HealthcheckOid")
    }

    override fun getMeta(): RecordMeta<*> {
        return META
    }
}
