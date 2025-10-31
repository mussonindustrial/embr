package com.mussonindustrial.embr.snmp.utils

import java.text.ParseException
import org.eclipse.milo.opcua.sdk.server.Lifecycle
import org.eclipse.milo.opcua.sdk.server.LifecycleManager
import org.eclipse.milo.opcua.stack.core.StatusCodes
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant
import org.snmp4j.PDU
import org.snmp4j.SNMP4JSettings
import org.snmp4j.Snmp
import org.snmp4j.Target
import org.snmp4j.smi.Address
import org.snmp4j.smi.Null
import org.snmp4j.smi.OID
import org.snmp4j.smi.OctetString
import org.snmp4j.smi.Variable
import org.snmp4j.smi.VariableBinding
import org.snmp4j.util.PDUFactory

fun String.toVariableBinding(): VariableBinding {
    return VariableBinding(OID(this))
}

fun PDU.addOID(oid: String) {
    val binding = VariableBinding(OID(oid))
    this.add(binding)
}

fun String.isOid(): Boolean {
    try {
        SNMP4JSettings.getOIDTextFormat().parse(this)
        return true
    } catch (_: ParseException) {
        return false
    }
}

fun Variable.toDataValue(): DataValue {
    return when (this) {
        Null.endOfMibView -> DataValue(StatusCodes.Bad_NotFound)
        Null.noSuchObject -> DataValue(StatusCodes.Bad_NotFound)
        Null.noSuchInstance -> DataValue(StatusCodes.Bad_NotFound)
        Null.instance -> DataValue(Variant.NULL_VALUE)
        else -> DataValue(Variant(this.toString()))
    }
}

fun DataValue.toVariable(): Variable {
    return OctetString(this.value.value?.toString())
}

fun LifecycleManager.addLifecycle(snmp: Snmp) {
    this.addLifecycle(
        object : Lifecycle {
            override fun startup() {
                snmp.listen()
            }

            override fun shutdown() {
                snmp.close()
            }
        }
    )
}

fun <A : Address> Target<A>.createSizeBoundedPDUs(
    pduFactory: PDUFactory,
    bindings: List<VariableBinding>,
    configure: PDU.() -> Unit = {},
): List<PDU> {

    val pdus = mutableListOf<PDU>()
    var pdu = pduFactory.createPDU(this).apply { configure(this) }

    bindings.forEach { binding ->
        pdu.add(binding)

        if (pdu.berLength > maxSizeRequestPDU) {
            pdu.trim()
            pdus.add(pdu)
            pdu = pduFactory.createPDU(this).apply { configure(this) }
        }
    }
    if (pdu.size() > 0) {
        pdus.add(pdu)
    }

    return pdus
}
