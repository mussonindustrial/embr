package com.mussonindustrial.embr.snmp.agents.scripting

import com.inductiveautomation.ignition.common.script.ScriptManager
import com.mussonindustrial.embr.common.scripting.asPyScriptExecutor
import com.mussonindustrial.embr.snmp.agents.rpc.SnmpAgentRpc
import com.mussonindustrial.embr.snmp.scripting.SnmpGatewayScriptMethodExecutor

class SnmpAgentGatewayScriptModule(rpc: SnmpAgentRpc, scriptManager: ScriptManager) :
    SnmpAgentScriptModule(
        rpc,
        RpcDelegateMethods(rpc),
        SnmpGatewayScriptMethodExecutor(scriptManager.asPyScriptExecutor()),
    )
