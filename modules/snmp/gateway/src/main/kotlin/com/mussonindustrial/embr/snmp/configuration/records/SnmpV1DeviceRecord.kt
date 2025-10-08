package com.mussonindustrial.embr.snmp.configuration.records

import com.inductiveautomation.ignition.gateway.localdb.persistence.*
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceSettingsRecord
import com.mussonindustrial.embr.snmp.configuration.settings.SnmpV1DeviceSettings
import simpleorm.dataset.SFieldFlags

class SnmpV1DeviceRecord : SnmpV1DeviceSettings, PersistentRecord() {

    companion object {
        val META = RecordMeta(SnmpV1DeviceRecord::class.java, "EmbrSnmpV1DeviceSettings")

        val DEVICE_SETTINGS_ID = LongField(META, "DeviceSettingsId", SFieldFlags.SPRIMARY_KEY)
        val DEVICE_SETTINGS =
            ReferenceField(META, DeviceSettingsRecord.META, "DeviceSettings", DEVICE_SETTINGS_ID)
                .apply { formMeta.isVisible = false }

        val ADDRESS = StringField(META, "Address", SFieldFlags.SMANDATORY)
        val TIMEOUT = IntField(META, "Timeout", SFieldFlags.SMANDATORY).apply { default = 1000 }
        val CATEGORY_CONNECTIVITY =
            Category("SnmpV1DeviceRecord.Connectivity", 1001).apply {
                include(ADDRESS)
                include(TIMEOUT)
            }

        val COMMUNITY_READ =
            StringField(META, "CommunityRead", SFieldFlags.SMANDATORY).apply { default = "public" }
        val COMMUNITY_WRITE = StringField(META, "CommunityWrite").apply { default = "private" }
        val CATEGORY_COMMUNITY =
            Category("SnmpV1DeviceRecord.Community", 1002).apply {
                include(COMMUNITY_READ)
                include(COMMUNITY_WRITE)
            }

        val HEALTHCHECK_FREQUENCY =
            IntField(META, "HealthcheckFrequency", SFieldFlags.SMANDATORY).apply { default = 10000 }
        val HEALTHCHECK_OID =
            StringField(META, "HealthcheckOid", SFieldFlags.SMANDATORY).apply {
                default = "1.3.6.1.2.1.1.2.0"
            }
        val CATEGORY_HEALTHCHECK =
            Category("SnmpV1DeviceRecord.Healthcheck", 1003, true).apply {
                include(HEALTHCHECK_FREQUENCY)
                include(HEALTHCHECK_OID)
            }
    }

    override fun getMeta(): RecordMeta<*> {
        return META
    }

    override val address: String
        get() = getString(ADDRESS)

    override val timeout: Int
        get() = getInt(TIMEOUT)

    override val communityRead: String
        get() = getString(COMMUNITY_READ)

    override val communityWrite: String?
        get() = getString(COMMUNITY_WRITE)

    override val healthcheckFrequency: Int
        get() = getInt(HEALTHCHECK_FREQUENCY)

    override val healthcheckOid: String
        get() = getString(HEALTHCHECK_OID)
}
