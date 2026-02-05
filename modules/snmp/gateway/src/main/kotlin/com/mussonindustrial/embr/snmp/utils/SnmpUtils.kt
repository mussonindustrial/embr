package com.mussonindustrial.embr.snmp.utils

import com.mussonindustrial.embr.snmp.model.BasicQualifiedOidValue
import com.mussonindustrial.embr.snmp.model.OidValue
import com.mussonindustrial.embr.snmp.model.QualifiedOidValue
import java.util.Date
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue

fun OidValue<DataValue>.toQualifiedValue(): QualifiedOidValue =
    BasicQualifiedOidValue(
        oid,
        this.value.value.value,
        this.value.statusCode.toQualityCode(),
        this.value.serverTime?.javaDate ?: Date(),
    )
