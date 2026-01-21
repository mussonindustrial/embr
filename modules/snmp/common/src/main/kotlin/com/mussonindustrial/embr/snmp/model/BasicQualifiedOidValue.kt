package com.mussonindustrial.embr.snmp.model

import com.inductiveautomation.ignition.common.TypeUtilities
import com.inductiveautomation.ignition.common.model.values.QualityCode
import java.io.InvalidObjectException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.*

data class BasicQualifiedOidValue(
    private var oid: Oid,
    private var value: Any?,
    private var quality: QualityCode = QualityCode.Bad_Stale,
    private var timeStamp: Date = Date(),
) : QualifiedOidValue, Serializable {

    companion object {
        private const val VERSION = 1
        private const val serialVersionUID: Long = 1L
    }

    private fun writeObject(out: ObjectOutputStream) {
        val payload =
            mapOf(
                "version" to VERSION,
                "value" to value,
                "oid" to oid.numeric,
                "quality" to quality.code,
                "timestamp" to timeStamp.time,
            )

        out.writeObject(payload)
    }

    @Suppress("UNCHECKED_CAST")
    private fun readObject(input: ObjectInputStream) {
        val payload = input.readObject() as Map<String, Any?>

        val version = (payload["version"] as? Number)?.toInt() ?: -1

        when (version) {
            1 -> {
                value = payload["value"]
                oid = Oid.fromNumeric(payload["oid"] as String)
                quality = QualityCode((payload["quality"] as Number).toInt())
                timeStamp = Date((payload["timestamp"] as Number).toLong())
            }
            else ->
                throw InvalidObjectException("Unsupported BasicQualifiedOidValue version: $version")
        }
    }

    override fun getOid(): Oid {
        return oid
    }

    override fun toString(): String {
        return String.format(
            "[%s, %s, %s, %s (%s)]",
            this.oid,
            this.value,
            this.quality,
            this.timestamp,
            this.timestamp.time,
        )
    }

    override fun getValue(): Any? {
        return value
    }

    fun setValue(value: Any?) {
        this.value = value
    }

    override fun getQuality(): QualityCode {
        return quality
    }

    fun setQuality(quality: QualityCode) {
        this.quality = quality
    }

    override fun getTimestamp(): Date {
        return timeStamp
    }

    fun setTimeStamp(timeStamp: Date) {
        this.timeStamp = timeStamp
    }

    override fun equals(value: Any?, includeTimestamp: Boolean): Boolean {
        if (value != null && BasicQualifiedOidValue::class.java.isAssignableFrom(value.javaClass)) {
            val other = value as BasicQualifiedOidValue
            return TypeUtilities.equals(this.quality, other.quality) &&
                TypeUtilities.deepEquals(this.value, other.value, true) &&
                TypeUtilities.equals(this.oid, other.oid) &&
                (!includeTimestamp || TypeUtilities.equals(this.timestamp, other.timestamp))
        } else {
            return false
        }
    }
}
