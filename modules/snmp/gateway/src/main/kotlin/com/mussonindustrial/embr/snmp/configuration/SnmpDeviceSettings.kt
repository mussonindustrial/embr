package com.mussonindustrial.embr.snmp.configuration

import com.inductiveautomation.ignition.gateway.localdb.persistence.*
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceSettingsRecord
import simpleorm.dataset.SFieldFlags

class SnmpDeviceSettings : PersistentRecord() {

    companion object {
        val META = RecordMeta(SnmpDeviceSettings::class.java, "SnmpDeviceSettings")
        val DEVICE_SETTINGS_ID = LongField(META, "DeviceSettingsId", SFieldFlags.SPRIMARY_KEY)
        val DEVICE_SETTINGS =
            ReferenceField(META, DeviceSettingsRecord.META, "DeviceSettings", DEVICE_SETTINGS_ID)
                .apply { formMeta.setVisible(false) }

        val HOSTNAME = StringField(META, "Hostname", SFieldFlags.SMANDATORY)
        val PORT = IntField(META, "Port", SFieldFlags.SMANDATORY).apply { default = 161 }
        val COMMUNITY =
            StringField(META, "Community", SFieldFlags.SMANDATORY).apply { default = "public" }
        val CONNECTION_TIMEOUT =
            LongField(META, "ConnectionTimeout", SFieldFlags.SMANDATORY).apply { default = 10000 }
        val CATEGORY_NETWORK =
            Category("SnmpDeviceSettings.Network", 1001).apply {
                include(HOSTNAME)
                include(PORT)
                include(COMMUNITY)
                include(CONNECTION_TIMEOUT)
            }

        val HEALTHCHECK_OID =
            StringField(META, "HealthcheckOid", SFieldFlags.SMANDATORY).apply {
                default = ".1.3.6.1.2.1.1.2.0"
            }
        val CATEGORY_HEALTHCHECK =
            Category("SnmpDeviceSettings.Healthcheck", 1002, true).apply {
                include(HEALTHCHECK_OID)
            }
    }

    override fun getMeta(): RecordMeta<*> {
        return META
    }

    val hostname: String
        get() = getString(HOSTNAME)

    val port: Int
        get() = getInt(PORT)

    val community: String
        get() = getString(COMMUNITY)

    val connectionTimeout: Long
        get() = getLong(CONNECTION_TIMEOUT)

    val healthcheckOid: String
        get() = getString(HEALTHCHECK_OID)
}
