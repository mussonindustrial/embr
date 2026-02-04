package com.mussonindustrial.embr.snmp.model

import com.mussonindustrial.embr.snmp.agents.devices.SnmpAgentDevice
import com.mussonindustrial.embr.snmp.opc.types.SnmpDataType
import java.util.concurrent.ConcurrentHashMap
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue
import org.snmp4j.smi.Variable

class ConcurrentObjectModel(val device: SnmpAgentDevice) : ObjectModel {

    private val descriptors = ConcurrentHashMap<Oid, ObjectModel.Descriptor>()
    private val knownValues = ConcurrentHashMap<Oid, OidValue<DataValue>>()

    override val oids: List<Oid>
        get() = descriptors.map { it.key }

    override fun observe(value: OidValue<Variable>): OidValue<DataValue> {
        val result = toOpcUaValue(value)
        knownValues[result.oid] = result

        descriptors.getOrPut(result.oid) {
            ObjectModel.ValueDescriptor(value.oid, SnmpDataType.of(value.value))
        }
        return result
    }

    override fun getDescriptors(oids: List<Oid>): List<ObjectModel.Descriptor> {
        return oids.map { descriptors.getOrDefault(it, ObjectModel.UnknownDescriptor(it)) }
    }
}
