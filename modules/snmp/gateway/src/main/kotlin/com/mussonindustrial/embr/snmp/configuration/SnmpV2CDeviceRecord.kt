package com.mussonindustrial.embr.snmp.configuration

import com.inductiveautomation.ignition.gateway.localdb.persistence.*
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceSettingsRecord
import simpleorm.dataset.SFieldFlags

class SnmpV2CDeviceRecord : SnmpV2CDeviceSettings, PersistentRecord() {

    companion object {
        val META = RecordMeta(SnmpV2CDeviceRecord::class.java, "EmbrSnmpV2CDeviceSettings")

        val DEVICE_SETTINGS_ID = LongField(META, "DeviceSettingsId", SFieldFlags.SPRIMARY_KEY)
        val DEVICE_SETTINGS =
            ReferenceField(META, DeviceSettingsRecord.META, "DeviceSettings", DEVICE_SETTINGS_ID)
                .apply { formMeta.isVisible = false }

        val HOSTNAME = StringField(META, "Hostname", SFieldFlags.SMANDATORY)
        val PORT = IntField(META, "Port", SFieldFlags.SMANDATORY).apply { default = 161 }
        val COMMUNITY =
            StringField(META, "Community", SFieldFlags.SMANDATORY).apply { default = "public" }
        val CONNECTION_TIMEOUT =
            LongField(META, "ConnectionTimeout", SFieldFlags.SMANDATORY).apply { default = 10000 }
        val CATEGORY_NETWORK =
            Category("SnmpV2CDeviceSettings.Network", 1001).apply {
                include(HOSTNAME)
                include(PORT)
                include(COMMUNITY)
                include(CONNECTION_TIMEOUT)
            }

        val HEALTHCHECK_OID =
            StringField(META, "HealthcheckOid", SFieldFlags.SMANDATORY).apply {
                default = "1.3.6.1.2.1.1.2.0"
            }
        val CATEGORY_HEALTHCHECK =
            Category("SnmpV2CDeviceSettings.Healthcheck", 1002, true).apply {
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

    override val community: String
        get() = getString(COMMUNITY)

    override val connectionTimeout: Long
        get() = getLong(CONNECTION_TIMEOUT)

    override val healthcheckOid: String
        get() = getString(HEALTHCHECK_OID)
}
