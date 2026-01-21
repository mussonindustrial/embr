package com.mussonindustrial.embr.common.scripting

import kotlin.reflect.KType

data class FunctionParameter(val name: String, val type: KType)

data class FunctionSignature(val parameters: List<FunctionParameter>)

class PyArgOverloadBuilder<T> {
    private var name = "anonymous"
    private val functions = mutableMapOf<FunctionSignature, (args: Map<String, Any?>) -> T>()

    fun setName(name: String): PyArgOverloadBuilder<T> {
        this.name = name
        return this
    }

    fun addOverload(
        function: (args: Map<String, Any?>) -> T,
        vararg args: Pair<String, KType>,
    ): PyArgOverloadBuilder<T> {
        val signature = FunctionSignature(args.map { FunctionParameter(it.first, it.second) })
        if (functions.containsKey(signature)) {
            val signatureString = signature.parameters.joinToString(",", "(", ")") { it.name }
            throw IllegalStateException("overload with signature $signatureString already exists")
        }
        functions[signature] = function
        return this
    }

    fun build(): PyArgOverload<T> {
        return PyArgOverload(name, functions)
    }
}
