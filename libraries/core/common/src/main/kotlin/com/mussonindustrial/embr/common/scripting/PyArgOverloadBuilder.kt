package com.mussonindustrial.embr.common.scripting

import kotlin.reflect.KType

data class FunctionParameter(val name: String, val type: KType)

data class FunctionSignature(val parameters: List<FunctionParameter>)

class PyArgOverloadBuilder {
    private val functions = mutableMapOf<FunctionSignature, (args: Array<Any?>) -> Any?>()
    private var name = "anonymous"

    fun setName(name: String): PyArgOverloadBuilder {
        this.name = name
        return this
    }

    fun addOverload(
        function: (args: Array<Any?>) -> Any?,
        vararg args: Pair<String, KType>,
    ): PyArgOverloadBuilder {
        val signature =
            FunctionSignature(
                args.map { FunctionParameter(it.first, it.second) },
            )
        if (functions.containsKey(signature)) {
            val signatureString = signature.parameters.joinToString(",", "(", ")") { it.name }
            throw IllegalStateException("overload with signature $signatureString already exists")
        }
        functions[signature] = function
        return this
    }

    fun build(): PyArgOverload {
        return PyArgOverload(name, functions)
    }
}
