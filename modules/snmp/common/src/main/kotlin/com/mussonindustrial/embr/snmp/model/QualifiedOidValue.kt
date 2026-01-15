package com.mussonindustrial.embr.snmp.model

import com.inductiveautomation.ignition.common.model.values.QualifiedValue
import org.snmp4j.smi.OID

interface QualifiedOidValue : QualifiedValue {
    fun getOid(): OID
}
