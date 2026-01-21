package com.mussonindustrial.embr.snmp.agents.expressions

import com.inductiveautomation.ignition.common.expressions.ExpressionFunctionManager
import com.mussonindustrial.embr.snmp.agents.scripting.SnmpAgentScriptModule
import com.mussonindustrial.embr.snmp.scripting.SnmpScriptMethodExecutor

class SnmpAgentExpressionFunctions(
    val methods: SnmpAgentScriptModule.Methods,
    val executor: SnmpScriptMethodExecutor,
) {
    fun configureFactory(factory: ExpressionFunctionManager) {

        factory.categories.add(SnmpAgentWalkExpressionFunction.CATEGORY)

        factory.addFunction(
            SnmpAgentReadExpressionFunction.NAME,
            SnmpAgentReadExpressionFunction.CATEGORY,
            SnmpAgentReadExpressionFunction(methods.read, executor),
        )
        factory.addFunction(
            SnmpAgentReadTableExpressionFunction.NAME,
            SnmpAgentReadTableExpressionFunction.CATEGORY,
            SnmpAgentReadTableExpressionFunction(methods.readTable, executor),
        )
        factory.addFunction(
            SnmpAgentWalkExpressionFunction.NAME,
            SnmpAgentWalkExpressionFunction.CATEGORY,
            SnmpAgentWalkExpressionFunction(methods.walk, executor),
        )
    }
}
