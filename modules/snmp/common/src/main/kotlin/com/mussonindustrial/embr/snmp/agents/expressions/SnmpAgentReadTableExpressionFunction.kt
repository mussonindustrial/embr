package com.mussonindustrial.embr.snmp.agents.expressions

import com.inductiveautomation.ignition.common.Dataset
import com.inductiveautomation.ignition.common.TypeUtilities
import com.inductiveautomation.ignition.common.expressions.Expression
import com.inductiveautomation.ignition.common.expressions.functions.AbstractFunction
import com.inductiveautomation.ignition.common.model.values.BasicQualifiedValue
import com.inductiveautomation.ignition.common.model.values.QualifiedValue
import com.mussonindustrial.embr.snmp.scripting.SnmpScriptMethod
import com.mussonindustrial.embr.snmp.scripting.SnmpScriptMethodExecutor
import org.python.core.Py

class SnmpAgentReadTableExpressionFunction(
    val method: SnmpScriptMethod<Dataset>,
    val executor: SnmpScriptMethodExecutor,
) : AbstractFunction() {

    companion object {
        const val NAME = "snmpReadTable"
        const val CATEGORY = "SNMP"
    }

    override fun getType(): Class<*> {
        return Dataset::class.java
    }

    override fun getArgDocString(): String {
        return "agent, column1, [column2, ...]"
    }

    override fun getFunctionDisplayName(): String {
        return NAME
    }

    override fun validateNumArgs(num: Int): Boolean {
        return (num >= 2)
    }

    override fun execute(expressions: Array<out Expression>): QualifiedValue {
        val agent = TypeUtilities.toString(expressions.first().execute().value)!!
        val oids = expressions.drop(1).map { TypeUtilities.toString(it.execute().value) }

        val args = arrayOf(Py.java2py(agent), Py.java2py(oids))

        val result = executor.executeBlocking(method, args, arrayOf())
        return BasicQualifiedValue(result)
    }
}
