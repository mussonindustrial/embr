package com.mussonindustrial.embr.snmp.agents.model

import com.mussonindustrial.embr.snmp.agents.devices.SnmpAgentDevice
import com.mussonindustrial.embr.snmp.model.BasicOidValue
import com.mussonindustrial.embr.snmp.model.Oid
import com.mussonindustrial.embr.snmp.model.OidValue
import com.mussonindustrial.embr.snmp.typing.snmpType
import java.util.concurrent.ConcurrentHashMap
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode
import org.snmp4j.smi.Variable

class ConcurrentObjectModel(val device: SnmpAgentDevice) : ObjectModel {

    private val descriptors = ConcurrentHashMap<Oid, ObjectModel.Descriptor>()
    private val knownValues = ConcurrentHashMap<Oid, OidValue<DataValue>>()

    override val oids: List<Oid>
        get() = descriptors.map { it.key }

    override fun read(reads: List<Oid>): List<OidValue<DataValue>> {
        return device.read(reads).map { coerceCache(it) }
    }

    override fun write(writes: List<Pair<Oid, Any?>>): List<OidValue<StatusCode>> {
        return device.write(
            writes.map { (oid, value) -> oid to toSnmpValue(BasicOidValue(oid, value)).value }
        )
    }

    override fun walk(roots: List<Oid>): List<OidValue<DataValue>> {
        return device.walk(roots).map { coerceCache(it) }
    }

    override fun readTable(
        columns: List<Oid>,
        lowerBoundIndex: Oid?,
        upperBoundIndex: Oid?,
    ): List<List<OidValue<DataValue>>> {
        return device.readTable(columns, lowerBoundIndex, upperBoundIndex).map {
            it.map { result -> coerceCache(result) }
        }
    }

    fun coerceCache(value: OidValue<Variable>): OidValue<DataValue> {
        val descriptor = ObjectModel.ValueDescriptor(value.oid, value.value.snmpType)
        val result = toOpcUaValue(value)

        descriptors[result.oid] = descriptor
        knownValues[result.oid] = result

        return result
    }

    override fun getDescriptors(oids: List<Oid>): List<ObjectModel.Descriptor> {
        val results = oids.map { descriptors.getOrDefault(it, ObjectModel.UnknownDescriptor(it)) }

        val missingDescriptors =
            oids
                .zip(results)
                .filter { (_, descriptor) -> descriptor is ObjectModel.UnknownDescriptor }
                .map { it.first }
        if (missingDescriptors.isEmpty()) return results

        read(missingDescriptors)
        return oids.map { descriptors.getOrDefault(it, ObjectModel.InvalidDescriptor(it)) }
    }
}
