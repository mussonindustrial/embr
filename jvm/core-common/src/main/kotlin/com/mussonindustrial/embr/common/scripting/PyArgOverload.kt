package com.mussonindustrial.embr.common.scripting

import com.inductiveautomation.ignition.common.TypeUtilities
import com.inductiveautomation.ignition.common.script.PyArgParser
import org.python.core.Py
import org.python.core.PyObject

class PyArgOverload(val name: String, private val functions: Map<FunctionSignature, (args: Array<Any>) -> Any?>) {

    fun call(args: Array<PyObject>, keywords: Array<String>): Any? {

        val signatures =  functions.keys.flatMap { signature -> signature.parameters }.toSet()

        val argParser = PyArgParser.parseArgs(
            args,
            keywords,
            signatures.map { it.name }.toTypedArray(),
            signatures.map { it.type.java }.toTypedArray(),
            this.name
        )

        functions.forEach { f ->
            val signature = f.key
            val function = f.value
            if (signature.parameters.all {
                    argParser.containsKey(it.name)
                }) {
                return function(signature.parameters.map {
                    val pyValue = argParser.getPyObject(it.name).get()
                    val jValue = TypeUtilities.pyToJava(pyValue)
                    TypeUtilities.coerce(jValue, it.type.java)
                }.toTypedArray())
            }
        }

        val validSignatures = functions.keys.joinToString(", ") { functionSignature ->
            functionSignature.parameters.joinToString(", ", "(", ")") { it.name }
        }
        val message = "No matching function signature found for '${name}'. Valid signatures include: $validSignatures"
        throw Py.TypeError(message)
    }

}