package com.mussonindustrial.embr.snmp.agents.opc.nodes

import com.mussonindustrial.embr.snmp.agents.devices.SnmpAgentDevice
import com.mussonindustrial.embr.snmp.model.asExtendedOid
import com.mussonindustrial.embr.snmp.opc.types.OidValueType
import kotlin.collections.toTypedArray
import org.eclipse.milo.opcua.sdk.server.methods.MethodInvocationHandler
import org.eclipse.milo.opcua.sdk.server.nodes.UaMethodNode
import org.eclipse.milo.opcua.sdk.server.nodes.UaNodeContext
import org.eclipse.milo.opcua.stack.core.NodeIds
import org.eclipse.milo.opcua.stack.core.types.builtin.DiagnosticInfo
import org.eclipse.milo.opcua.stack.core.types.builtin.ExtensionObject
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint
import org.eclipse.milo.opcua.stack.core.types.structured.Argument
import org.eclipse.milo.opcua.stack.core.types.structured.CallMethodResult

class WalkMethodNode(context: UaNodeContext, nodeId: NodeId, val device: SnmpAgentDevice) :
    UaMethodNode(
        context,
        nodeId,
        QualifiedName.parse("Walk"),
        LocalizedText.english("Walk"),
        LocalizedText.english("Walk the SNMP tree starting at the given OID."),
        uint(0),
        uint(0),
        true,
        true,
    ) {

    init {
        inputArguments =
            arrayOf(
                Argument(
                    "Roots",
                    NodeIds.String,
                    1,
                    arrayOf(UInteger.valueOf(0)),
                    LocalizedText.english("A list of OIDs to walk."),
                )
            )

        outputArguments =
            arrayOf(
                Argument(
                    "Result",
                    OidValueType.TYPE_ID.toNodeId(nodeContext.namespaceTable).get(),
                    1,
                    arrayOf(UInteger.valueOf(0)),
                    LocalizedText.english("A list of values discovered during the walk operation."),
                )
            )

        invocationHandler = MethodInvocationHandler { _, request ->
            val roots = request.inputArguments?.first()?.value as Array<*>

            val results =
                device
                    .walk(roots.map { (it as String).asExtendedOid() })
                    .map {
                        ExtensionObject.encode(
                            context.server.staticEncodingContext,
                            OidValueType(it.oid.numeric, it.value.value.value),
                        )
                    }
                    .toTypedArray()

            val output = Variant(results)

            CallMethodResult(
                StatusCode.GOOD,
                arrayOfNulls<StatusCode>(0),
                arrayOfNulls<DiagnosticInfo>(0),
                arrayOf(output),
            )
        }
    }
}
