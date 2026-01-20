package com.mussonindustrial.embr.snmp.agents.scripting

import com.inductiveautomation.ignition.common.BundleUtil
import com.inductiveautomation.ignition.common.script.ScriptManager
import com.mussonindustrial.embr.common.scripting.asPyScriptExecutor
import com.mussonindustrial.embr.snmp.agents.rpc.SnmpAgentRpc
import com.mussonindustrial.embr.snmp.scripting.ClientScriptMethodFactory

class SnmpAgentClientScriptModule(rpc: SnmpAgentRpc, scriptManager: ScriptManager) :
    SnmpAgentScriptModule(
        ClientScriptMethodFactory(scriptManager.asPyScriptExecutor()).run {
            val overloads = SnmpAgentScriptOverloads(rpc)
            Methods(
                read = create(overloads.read),
                write = create(overloads.write),
                walk = create(overloads.walk),
                readTable = create(overloads.readTable),
            )
        }
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
