package com.mussonindustrial.embr.snmp.model

import com.mussonindustrial.embr.snmp.opc.toQualityCode
import java.util.Date
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue

fun OidValue<DataValue>.toQualifiedValue(): QualifiedOidValue =
    BasicQualifiedOidValue(
        oid,
        this.value.value.value,
        this.value.statusCode.toQualityCode(),
        this.value.serverTime?.javaDate ?: Date(),
    )
