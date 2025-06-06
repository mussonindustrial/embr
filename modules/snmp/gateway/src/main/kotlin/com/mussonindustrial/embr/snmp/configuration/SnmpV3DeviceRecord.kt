package com.mussonindustrial.embr.snmp.configuration

import com.inductiveautomation.ignition.gateway.localdb.persistence.*
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceSettingsRecord
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
        val CONNECTION_TIMEOUT =
            LongField(META, "ConnectionTimeout", SFieldFlags.SMANDATORY).apply { default = 10000 }
        val CATEGORY_NETWORK =
            Category("SnmpV3DeviceSettings.Network", 1001).apply {
                include(HOSTNAME)
                include(PORT)
                include(CONNECTION_TIMEOUT)
            }

        val HEALTHCHECK_OID =
            StringField(META, "HealthcheckOid", SFieldFlags.SMANDATORY).apply {
                default = "1.3.6.1.2.1.1.2.0"
            }
        val CATEGORY_HEALTHCHECK =
            Category("SnmpV3DeviceSettings.Healthcheck", 1002, true).apply {
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

    override val connectionTimeout: Long
        get() = getLong(CONNECTION_TIMEOUT)

    override val healthcheckOid: String
        get() = getString(HEALTHCHECK_OID)
}
