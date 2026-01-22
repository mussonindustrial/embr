package com.mussonindustrial.embr.snmp.agents.context

import com.mussonindustrial.embr.snmp.agents.devices.SnmpAgentDevice
import com.mussonindustrial.embr.snmp.model.BasicOidValue
import com.mussonindustrial.embr.snmp.model.Oid
import com.mussonindustrial.embr.snmp.model.OidValue
import com.mussonindustrial.embr.snmp.typing.snmpType
import java.util.concurrent.ConcurrentHashMap
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode
import org.snmp4j.smi.Variable

class ConcurrentOidModel(val device: SnmpAgentDevice) : OidModel {

    private val descriptors = ConcurrentHashMap<Oid, OidModel.Descriptor>()
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
        val descriptor = OidModel.ValueDescriptor(value.oid, value.value.snmpType)
        val result = toOpcUaValue(value)

        descriptors[result.oid] = descriptor
        knownValues[result.oid] = result

        return result
    }

    override fun getDescriptors(oids: List<Oid>): List<OidModel.Descriptor> {
        val results = oids.map { descriptors.getOrDefault(it, OidModel.UnknownDescriptor(it)) }

        val missingDescriptors =
            oids
                .zip(results)
                .filter { (_, descriptor) -> descriptor is OidModel.UnknownDescriptor }
                .map { it.first }
        if (missingDescriptors.isEmpty()) return results

        read(missingDescriptors)
        return oids.map { descriptors.getOrDefault(it, OidModel.InvalidDescriptor(it)) }
    }
}
