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

        val HOSTNAME = StringField(META, "Hostname", SFieldFlags.SMANDATORY)
        val PORT = IntField(META, "Port", SFieldFlags.SMANDATORY).apply { default = 161 }
        val COMMUNITY_READ =
            StringField(META, "CommunityRead", SFieldFlags.SMANDATORY).apply { default = "public" }
        val COMMUNITY_WRITE =
            StringField(META, "CommunityWrite", SFieldFlags.SMANDATORY).apply { default = "public" }
        val CATEGORY_NETWORK =
            Category("SnmpV1DeviceRecord.Network", 1001).apply {
                include(HOSTNAME)
                include(PORT)
                include(COMMUNITY_READ)
                include(COMMUNITY_WRITE)
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
            Category("SnmpV1DeviceRecord.Healthcheck", 1002, true).apply {
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

    override val communityRead: String
        get() = getString(COMMUNITY_READ)

    override val communityWrite: String
        get() = getString(COMMUNITY_WRITE)

    override val healthcheckFrequency: Long
        get() = getLong(HEALTHCHECK_FREQUENCY)

    override val healthcheckOid: String
        get() = getString(HEALTHCHECK_OID)
}
