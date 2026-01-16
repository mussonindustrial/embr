package com.mussonindustrial.embr.snmp.agents.scripting

import com.mussonindustrial.embr.common.scripting.PyArgOverloadBuilder
import com.mussonindustrial.embr.snmp.agents.rpc.SnmpAgentRpc
import kotlin.reflect.typeOf

class SnmpAgentScriptOverloads(val impl: SnmpAgentRpc) {

    val read =
        PyArgOverloadBuilder()
            .setName("read")
            .addOverload(
                @Suppress("UNCHECKED_CAST") {
                    val agent = it["agent"] as String
                    val oids = it["oids"] as List<String>
                    impl.read(agent, oids)
                },
                "agent" to typeOf<String>(),
                "oids" to typeOf<List<String>>(),
            )
            .build()

    val write =
        PyArgOverloadBuilder()
            .setName("write")
            .addOverload(
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
            .build()

    val walk =
        PyArgOverloadBuilder()
            .setName("walk")
            .addOverload(
                @Suppress("UNCHECKED_CAST") {
                    val agent = it["agent"] as String
                    val oids = it["oids"] as List<String>
                    impl.walk(agent, oids)
                },
                "agent" to typeOf<String>(),
                "oids" to typeOf<List<String>>(),
            )
            .build()

    val readTable =
        PyArgOverloadBuilder()
            .setName("readTable")
            .addOverload(
                @Suppress("UNCHECKED_CAST") {
                    val agent = it["agent"] as String
                    val columns = it["columns"] as List<String>
                    val lowerBoundIndex = it["lowerBoundIndex"] as? String?
                    val upperBoundIndex = it["upperBoundIndex"] as? String?
                    impl.readTable(agent, columns, lowerBoundIndex, upperBoundIndex)
                },
                "agent" to typeOf<String>(),
                "columns" to typeOf<List<String>>(),
                "lowerBoundIndex" to typeOf<String?>(),
                "upperBoundIndex" to typeOf<String?>(),
            )
            .build()
}
