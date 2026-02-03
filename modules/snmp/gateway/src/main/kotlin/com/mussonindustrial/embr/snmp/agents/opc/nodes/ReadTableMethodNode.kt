package com.mussonindustrial.embr.snmp.agents.opc.nodes

import com.mussonindustrial.embr.snmp.agents.devices.SnmpAgentDevice
import com.mussonindustrial.embr.snmp.model.asExtendedOid
import com.mussonindustrial.embr.snmp.model.nullOrExtendedOid
import com.mussonindustrial.embr.snmp.opc.types.OidValueType
import kotlin.collections.toTypedArray
import org.eclipse.milo.opcua.sdk.core.ValueRank
import org.eclipse.milo.opcua.sdk.server.methods.MethodInvocationHandler
import org.eclipse.milo.opcua.sdk.server.nodes.UaMethodNode
import org.eclipse.milo.opcua.sdk.server.nodes.UaNodeContext
import org.eclipse.milo.opcua.stack.core.NodeIds
import org.eclipse.milo.opcua.stack.core.types.builtin.DiagnosticInfo
import org.eclipse.milo.opcua.stack.core.types.builtin.ExtensionObject
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText
import org.eclipse.milo.opcua.stack.core.types.builtin.Matrix
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint
import org.eclipse.milo.opcua.stack.core.types.structured.Argument
import org.eclipse.milo.opcua.stack.core.types.structured.CallMethodResult

class ReadTableMethodNode(context: UaNodeContext, nodeId: NodeId, val device: SnmpAgentDevice) :
    UaMethodNode(
        context,
        nodeId,
        QualifiedName.parse("ReadTable"),
        LocalizedText.english("ReadTable"),
        LocalizedText.english("Read from an SNMP table."),
        uint(0),
        uint(0),
        true,
        true,
    ) {

    init {
        inputArguments =
            arrayOf(
                Argument(
                    "Columns",
                    NodeIds.String,
                    ValueRank.OneDimension.value,
                    arrayOf(UInteger.valueOf(0)),
                    LocalizedText.english("A list of column OIDs that define the table structure."),
                ),
                Argument(
                    "LowerBoundIndex",
                    NodeIds.String,
                    ValueRank.Scalar.value,
                    null,
                    LocalizedText.english("The inclusive lower index bound for the table read."),
                ),
                Argument(
                    "UpperBoundIndex",
                    NodeIds.String,
                    ValueRank.Scalar.value,
                    null,
                    LocalizedText.english("The inclusive upper index bound for the table read."),
                ),
            )

        outputArguments =
            arrayOf(
                Argument(
                    "Result",
                    OidValueType.TYPE_ID.toNodeId(nodeContext.namespaceTable).get(),
                    2,
                    arrayOf(UInteger.valueOf(0), UInteger.valueOf(0)),
                    LocalizedText.english(
                        "An array containing the table rows and columns derived from the SNMP response."
                    ),
                )
            )

        invocationHandler = MethodInvocationHandler { _, request ->
            val columns =
                request.inputArguments?.first()?.value as Array<*>?
                    ?: return@MethodInvocationHandler CallMethodResult(
                        StatusCode.BAD,
                        arrayOfNulls<StatusCode>(0),
                        arrayOfNulls<DiagnosticInfo>(0),
                        arrayOf(),
                    )

            val lowerBoundIndex = request.inputArguments?.get(1)?.value as String
            val upperBoundIndex = request.inputArguments?.get(2)?.value as String

            val results =
                device
                    .readTable(
                        columns.map { (it as String).asExtendedOid() },
                        lowerBoundIndex.ifEmpty { null }.nullOrExtendedOid(),
                        upperBoundIndex.ifEmpty { null }.nullOrExtendedOid(),
                    )
                    .map { column ->
                        column
                            .map {
                                ExtensionObject.encode(
                                    context.server.staticEncodingContext,
                                    OidValueType(it.oid.numeric, it.value.value.value),
                                )
                            }
                            .toTypedArray()
                    }
                    .toTypedArray()

            val output = Variant(Matrix(results))

            CallMethodResult(
                StatusCode.GOOD,
                arrayOfNulls<StatusCode>(0),
                arrayOfNulls<DiagnosticInfo>(0),
                arrayOf(output),
            )
        }
    }
}
