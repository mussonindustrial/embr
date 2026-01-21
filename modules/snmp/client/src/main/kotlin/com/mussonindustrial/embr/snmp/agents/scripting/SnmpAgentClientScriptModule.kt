package com.mussonindustrial.embr.snmp.agents.scripting

import com.inductiveautomation.ignition.common.BundleUtil
import com.inductiveautomation.ignition.common.script.ScriptManager
import com.mussonindustrial.embr.common.scripting.asPyScriptExecutor
import com.mussonindustrial.embr.snmp.agents.rpc.SnmpAgentRpc
import com.mussonindustrial.embr.snmp.scripting.SnmpClientScriptMethodExecutor

class SnmpAgentClientScriptModule(rpc: SnmpAgentRpc, scriptManager: ScriptManager) :
    SnmpAgentScriptModule(
        rpc,
        RpcDelegateMethods(rpc),
        SnmpClientScriptMethodExecutor(scriptManager.asPyScriptExecutor()),
    ) {

    companion object {
        init {
            BundleUtil.get()
                .addBundle(
                    SnmpAgentClientScriptModule::class.java.getSimpleName(),
                    SnmpAgentClientScriptModule::class.java.getClassLoader(),
                    SnmpAgentClientScriptModule::class.java.getName().replace('.', '/'),
                )
        }
    }
}
