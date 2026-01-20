package com.mussonindustrial.embr.snmp.agents.scripting

import com.inductiveautomation.ignition.common.script.ScriptManager
import com.mussonindustrial.embr.common.scripting.asPyScriptExecutor
import com.mussonindustrial.embr.snmp.SnmpGatewayContext
import com.mussonindustrial.embr.snmp.agents.rpc.SnmpAgentRpcImpl
import com.mussonindustrial.embr.snmp.scripting.SnmpGatewayScriptExecutor

class SnmpAgentGatewayScriptModule(context: SnmpGatewayContext, scriptManager: ScriptManager) :
    SnmpAgentScriptModule(
        RpcDelegateMethods(SnmpAgentRpcImpl(context)),
        SnmpGatewayScriptExecutor(scriptManager.asPyScriptExecutor()),
    )
