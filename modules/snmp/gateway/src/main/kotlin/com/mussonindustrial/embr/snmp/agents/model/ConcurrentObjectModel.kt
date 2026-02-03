package com.mussonindustrial.embr.snmp.agents.model

import com.mussonindustrial.embr.snmp.agents.devices.SnmpAgentDevice
import com.mussonindustrial.embr.snmp.model.Oid
import com.mussonindustrial.embr.snmp.model.OidValue
import com.mussonindustrial.embr.snmp.typing.snmpDataType
import java.util.concurrent.ConcurrentHashMap
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue
import org.snmp4j.smi.Variable

class ConcurrentObjectModel(val device: SnmpAgentDevice) : ObjectModel {

    private val descriptors = ConcurrentHashMap<Oid, ObjectModel.Descriptor>()
    private val knownValues = ConcurrentHashMap<Oid, OidValue<DataValue>>()

    override val oids: List<Oid>
        get() = descriptors.map { it.key }

    override fun observe(value: OidValue<Variable>): OidValue<DataValue> {
        val descriptor = ObjectModel.ValueDescriptor(value.oid, value.value.snmpDataType)
        val result = toOpcUaValue(value)

        descriptors[result.oid] = descriptor
        knownValues[result.oid] = result

        return result
    }

    override fun getDescriptors(oids: List<Oid>): List<ObjectModel.Descriptor> {
        return oids.map { descriptors.getOrDefault(it, ObjectModel.UnknownDescriptor(it)) }
    }
}
