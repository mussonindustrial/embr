package com.mussonindustrial.embr.snmp.agents.scripting

import com.inductiveautomation.ignition.common.Dataset
import com.inductiveautomation.ignition.common.model.values.QualityCode
import com.mussonindustrial.embr.snmp.agents.rpc.SnmpAgentRpc
import com.mussonindustrial.embr.snmp.model.QualifiedOidValue
import com.mussonindustrial.embr.snmp.model.toDataset
import com.mussonindustrial.embr.snmp.scripting.SnmpScriptOverload
import kotlin.reflect.typeOf

class SnmpAgentScriptOverloads(val impl: SnmpAgentRpc) {

    val read =
        SnmpScriptOverload.of<List<QualifiedOidValue>>(name = "read", isWrite = false) {
            addOverload(
                @Suppress("UNCHECKED_CAST") {
                    val agent = it["agent"] as String
                    val oids = it["oids"] as List<String>
                    impl.read(agent, oids)
                },
                "agent" to typeOf<String>(),
                "oids" to typeOf<List<String>>(),
            )
        }

    val write =
        SnmpScriptOverload.of<List<QualityCode>>(name = "write", isWrite = true) {
            addOverload(
                @Suppress("UNCHECKED_CAST") {
                    val agent = it["agent"] as String
                    val oids = it["oids"] as List<String>
                    val values = it["values"] as List<String>
                    impl.write(agent, oids, values)
                },
                "agent" to typeOf<String>(),
                "oids" to typeOf<List<String>>(),
                "values" to typeOf<List<String>>(),
            )
        }

    val walk =
        SnmpScriptOverload.of<List<QualifiedOidValue>>(name = "walk", isWrite = false) {
            addOverload(
                @Suppress("UNCHECKED_CAST") {
                    val agent = it["agent"] as String
                    val oids = it["oids"] as List<String>
                    impl.walk(agent, oids)
                },
                "agent" to typeOf<String>(),
                "oids" to typeOf<List<String>>(),
            )
        }

    val readTable =
        SnmpScriptOverload.of<Dataset>(name = "readTable", isWrite = false) {
            addOverload(
                @Suppress("UNCHECKED_CAST") {
                    val agent = it["agent"] as String
                    val columns = it["columns"] as List<String>
                    val lowerBoundIndex = it["lowerBoundIndex"] as? String?
                    val upperBoundIndex = it["upperBoundIndex"] as? String?
                    val result = impl.readTable(agent, columns, lowerBoundIndex, upperBoundIndex)
                    result.toDataset()
                },
                "agent" to typeOf<String>(),
                "columns" to typeOf<List<String>>(),
                "lowerBoundIndex" to typeOf<String?>(),
                "upperBoundIndex" to typeOf<String?>(),
            )
        }
}
