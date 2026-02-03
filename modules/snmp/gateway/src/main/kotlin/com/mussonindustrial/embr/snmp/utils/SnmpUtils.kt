package com.mussonindustrial.embr.snmp.utils

import com.mussonindustrial.embr.snmp.model.BasicQualifiedOidValue
import com.mussonindustrial.embr.snmp.model.OidValue
import com.mussonindustrial.embr.snmp.model.QualifiedOidValue
import java.text.ParseException
import java.util.Date
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue
import org.snmp4j.PDU
import org.snmp4j.SNMP4JSettings
import org.snmp4j.Target
import org.snmp4j.smi.Address
import org.snmp4j.smi.VariableBinding
import org.snmp4j.util.PDUFactory

fun String.isOid(): Boolean {
    try {
        SNMP4JSettings.getOIDTextFormat().parse(this)
        return true
    } catch (_: ParseException) {
        return false
    }
}

fun <A : Address> Target<A>.createSizeBoundedPDUs(
    pduFactory: PDUFactory,
    bindings: List<VariableBinding>,
    configure: PDU.() -> Unit = {},
): List<PDU> {

    val pdus = mutableListOf<PDU>()
    var pdu = pduFactory.createPDU(this).apply { configure(this) }
    var count = 0

    bindings.forEach { binding ->
        pdu.add(binding)
        count++

        if (pdu.berLength > maxSizeRequestPDU || count > 50) {
            pdu.trim()
            pdus.add(pdu)
            pdu = pduFactory.createPDU(this).apply { configure(this) }
            pdu.add(binding)
            count = 1
        }
    }
    if (pdu.size() > 0) {
        pdus.add(pdu)
    }

    return pdus
}

fun OidValue<DataValue>.toQualifiedValue(): QualifiedOidValue =
    BasicQualifiedOidValue(
        oid,
        this.value.value.value,
        this.value.statusCode.toQualityCode(),
        this.value.serverTime?.javaDate ?: Date(),
    )
