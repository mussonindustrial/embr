package com.mussonindustrial.ignition.embr.periscope.expressions

import com.inductiveautomation.ignition.common.binding.InteractionListener
import com.inductiveautomation.ignition.common.expressions.Expression
import com.inductiveautomation.ignition.common.expressions.ExpressionException
import com.inductiveautomation.ignition.common.expressions.functions.AbstractFunction
import com.inductiveautomation.ignition.common.model.CommonContext
import com.inductiveautomation.ignition.common.model.values.BasicQualifiedValue
import com.inductiveautomation.ignition.common.model.values.QualifiedValue
import com.mussonindustrial.ignition.embr.periscope.pystore.PerspectivePyStores
import com.mussonindustrial.ignition.embr.periscope.pystore.PyStorePath
import com.mussonindustrial.ignition.embr.periscope.pystore.PyStoreReadResult
import com.mussonindustrial.ignition.embr.periscope.utils.toPyValue

class PyStoreExpression(private val stores: PerspectivePyStores) : AbstractFunction() {

    companion object {
        const val NAME = "pyStore"
        const val CATEGORY = "Advanced"
    }

    private var updateListener: InteractionListener? = null
    private var subscription: AutoCloseable? = null

    override fun copy() = PyStoreExpression(stores)

    override fun getArgDocString() = "name, path [, scope]"

    override fun getFunctionDisplayName() = NAME

    override fun getType(): Class<*> = Any::class.java

    override fun validateNumArgs(num: Int) = num in 1..3

    override fun connect(
        context: CommonContext,
        updateListener: InteractionListener,
    ) {
        super.connect(context, updateListener)
        this.updateListener = updateListener
    }

    override fun disconnect() {
        unsubscribe()
        updateListener = null
        super.disconnect()
    }

    override fun execute(args: Array<Expression>): QualifiedValue {
        unsubscribe()

        val name = stringArg(args[0], "name")

        val pathString = args.getOrNull(1)?.let { stringArg(it, "path") } ?: ""
        val path = parsePath(pathString)

        val scope = args.getOrNull(2)?.let { stringArg(it, "scope") } ?: "page"
        val store = resolveStore(name, scope)

        updateListener?.let {
            subscription =
                stores.subscribe(
                    owner = this,
                    store = store,
                    path = path,
                    listener = it,
                )
        }

        return BasicQualifiedValue(
            when (val result = store.expressionValue(path)) {
                is PyStoreReadResult.Resolved -> result.value.toPyValue()
                PyStoreReadResult.Unresolved -> null
            }
        )
    }

    private fun parsePath(path: String): PyStorePath =
        try {
            PyStorePath.parse(path)
        } catch (exception: Exception) {
            throw ExpressionException("Invalid PyStore path '$path'", exception)
        }

    private fun resolveStore(name: String, scope: String) =
        try {
            stores.getCurrent(name, scope)
        } catch (exception: Exception) {
            throw ExpressionException("Unable to resolve $scope PyStore '$name'", exception)
        }

    private fun stringArg(expression: Expression, name: String): String {
        val value = expression.execute().value

        return value as? String
            ?: throw ExpressionException("$NAME() argument '$name' must be a string")
    }

    private fun unsubscribe() {
        subscription?.close()
        subscription = null
    }
}
