package com.mussonindustrial.embr.snmp.agents.model

import com.mussonindustrial.embr.snmp.model.BasicOidValue
import com.mussonindustrial.embr.snmp.model.Oid
import com.mussonindustrial.embr.snmp.model.OidValue
import com.mussonindustrial.embr.snmp.model.SnmpCommunicationError
import com.mussonindustrial.embr.snmp.typing.SnmpType
import org.eclipse.milo.opcua.stack.core.StatusCodes
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.ULong
import org.snmp4j.smi.Counter64
import org.snmp4j.smi.Integer32
import org.snmp4j.smi.Null
import org.snmp4j.smi.OID
import org.snmp4j.smi.OctetString
import org.snmp4j.smi.SMIAddress
import org.snmp4j.smi.UnsignedInteger32
import org.snmp4j.smi.Variable

interface ObjectModel {

    val oids: List<Oid>

    fun read(reads: List<Oid>): List<OidValue<DataValue>>

    fun write(writes: List<Pair<Oid, Any?>>): List<OidValue<StatusCode>>

    fun walk(roots: List<Oid>): List<OidValue<DataValue>>

    fun readTable(
        columns: List<Oid>,
        lowerBoundIndex: Oid?,
        upperBoundIndex: Oid?,
    ): List<List<OidValue<DataValue>>>

    fun getDescriptors(oids: List<Oid>): List<Descriptor>

    fun toSnmpValue(value: OidValue<Any?>): OidValue<Variable> {
        val descriptor = getDescriptors(listOf(value.oid)).first()

        val snmpValue =
            when (descriptor) {
                is ValueDescriptor -> descriptor.snmpType.variableOfType(value.value)
                is InvalidDescriptor -> Null.instance
                is TableColumnDescriptor -> Null.instance
                is TableDescriptor -> Null.instance
                is UnknownDescriptor -> Null.instance
            }
        return BasicOidValue(value.oid, snmpValue)
    }

    fun toOpcUaValue(value: OidValue<Variable>): OidValue<DataValue> {
        if (value.value == SnmpCommunicationError) {
            return BasicOidValue(value.oid, DataValue(StatusCodes.Bad_CommunicationError))
        }

        val opcUaValue =
            when (value.value) {
                Null.endOfMibView -> DataValue(StatusCodes.Bad_NotFound)
                Null.noSuchObject -> DataValue(StatusCodes.Bad_NotFound)
                Null.noSuchInstance -> DataValue(StatusCodes.Bad_NotFound)
                Null.instance -> DataValue(Variant.NULL_VALUE)
                is Integer32 -> DataValue(Variant.ofInt32(value.value.toInt()))
                is UnsignedInteger32 -> DataValue(Variant.ofInt32(value.value.toInt()))
                is Counter64 -> DataValue(Variant.ofUInt64(ULong.valueOf(value.value.toLong())))
                is SMIAddress -> DataValue(Variant.ofString(value.value.toString()))
                is OctetString -> DataValue(Variant.ofString(value.value.toString()))
                is OID -> DataValue(Variant.ofString(value.value.toString()))
                else -> DataValue(Variant.ofString(value.value.toString()))
            }
        return BasicOidValue(value.oid, opcUaValue)
    }

    sealed class Descriptor(val oid: Oid)

    class InvalidDescriptor(oid: Oid) : Descriptor(oid)

    class UnknownDescriptor(oid: Oid) : Descriptor(oid)

    open class ValueDescriptor(oid: Oid, val snmpType: SnmpType) : Descriptor(oid)

    class TableColumnDescriptor(oid: Oid, val snmpType: SnmpType) : Descriptor(oid)

    class TableDescriptor(oid: Oid, val columns: List<TableColumnDescriptor>) : Descriptor(oid)
}
