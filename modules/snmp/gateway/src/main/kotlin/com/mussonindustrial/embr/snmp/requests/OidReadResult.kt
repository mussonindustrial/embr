package com.mussonindustrial.embr.snmp.requests

import com.mussonindustrial.embr.snmp.model.BasicQualifiedOidValue
import com.mussonindustrial.embr.snmp.model.QualifiedOidValue
import com.mussonindustrial.embr.snmp.utils.toDataValue
import com.mussonindustrial.embr.snmp.utils.toQualityCode
import java.util.Date
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue
import org.snmp4j.smi.OID
import org.snmp4j.smi.VariableBinding

data class OidReadResult(val oid: OID, val value: DataValue) {
    fun toQualifiedValue(): QualifiedOidValue {
        return BasicQualifiedOidValue(
            this.oid,
            this.value.value.value(),
            this.value.statusCode.toQualityCode(),
            this.value.serverTime?.javaDate ?: Date(),
        )
    }
}

fun OID.toOidReadResult(value: DataValue): OidReadResult {
    return OidReadResult(this, value)
}

fun VariableBinding.toOidReadResult(): OidReadResult {
    return OidReadResult(this.oid, this.variable.toDataValue())
}
