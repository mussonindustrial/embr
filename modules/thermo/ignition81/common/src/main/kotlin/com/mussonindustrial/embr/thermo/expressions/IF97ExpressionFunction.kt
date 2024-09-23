package com.mussonindustrial.embr.thermo.expressions

import com.inductiveautomation.ignition.common.TypeUtilities
import com.inductiveautomation.ignition.common.expressions.Expression
import com.inductiveautomation.ignition.common.expressions.functions.AbstractFunction
import com.inductiveautomation.ignition.common.model.values.BasicQualifiedValue
import com.inductiveautomation.ignition.common.model.values.QualifiedValue
import com.mussonindustrial.embr.thermo.IF97PyArgOverloads
import org.python.core.Py

class IF97ExpressionFunction : AbstractFunction() {

    companion object {
        const val NAME = "if97"
        const val CATEGORY = "Thermo"
    }

    override fun getType(): Class<*> {
        return Double::class.java
    }

    override fun getArgDocString(): String {
        return "property, [[parameter], [value]...]"
    }

    override fun getFunctionDisplayName(): String {
        return NAME
    }

    override fun validateNumArgs(num: Int): Boolean {
        return (num % 2) == 1
    }

    override fun execute(expressions: Array<out Expression>): QualifiedValue {
        val property = TypeUtilities.toString(expressions.first().execute().value)!!
        val parameters =
            expressions.drop(1).map { it.execute().value }.chunked(2).map { it[0] to it[1] }

        val keywords = parameters.map { TypeUtilities.toString(it.first)!! }.toTypedArray()
        val args = parameters.map { Py.java2py(it.second) }.toTypedArray()

        val f = IF97PyArgOverloads.getFunction(property)
        val result = f.call(args, keywords)

        return BasicQualifiedValue(result)
    }
}
