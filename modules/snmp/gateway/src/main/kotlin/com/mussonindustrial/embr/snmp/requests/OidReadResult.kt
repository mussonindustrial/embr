package com.mussonindustrial.embr.snmp.requests

import com.inductiveautomation.ignition.common.model.values.BasicQualifiedValue
import com.inductiveautomation.ignition.common.model.values.QualifiedValue
import com.mussonindustrial.embr.snmp.utils.toQualityCode
import java.util.Date
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue

data class OidReadResult(val value: DataValue) {
    fun toQualifiedValue(): QualifiedValue {
        return BasicQualifiedValue(
            this.value.value.value(),
            this.value.statusCode.toQualityCode(),
            this.value.serverTime?.javaDate ?: Date(),
        )
    }
}

fun DataValue.toOidReadResult(): OidReadResult {
    return OidReadResult(this)
}
