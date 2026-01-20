package com.mussonindustrial.embr.snmp.agents.scripting

import com.inductiveautomation.ignition.common.script.ScriptManager
import com.mussonindustrial.embr.common.scripting.asPyScriptExecutor
import com.mussonindustrial.embr.snmp.SnmpGatewayContext
import com.mussonindustrial.embr.snmp.agents.rpc.SnmpAgentRpcImpl
import com.mussonindustrial.embr.snmp.scripting.GatewayScriptMethodFactory

class SnmpAgentGatewayScriptModule(context: SnmpGatewayContext, scriptManager: ScriptManager) :
    SnmpAgentScriptModule(
        GatewayScriptMethodFactory(scriptManager.asPyScriptExecutor()).run {
            val overloads = SnmpAgentScriptOverloads(SnmpAgentRpcImpl(context))
            Methods(
                read = create(overloads.read),
                write = create(overloads.write),
                walk = create(overloads.walk),
                readTable = create(overloads.readTable),
            )
        }
    )
