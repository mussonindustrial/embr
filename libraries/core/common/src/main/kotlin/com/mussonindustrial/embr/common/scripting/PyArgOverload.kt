package com.mussonindustrial.embr.common.scripting

import com.inductiveautomation.ignition.common.TypeUtilities
import com.inductiveautomation.ignition.common.script.PyArgParser
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.javaType
import org.python.core.Py
import org.python.core.PyObject

class PyArgOverload(
    val name: String,
    private val functions: Map<FunctionSignature, (args: Map<String, Any?>) -> Any?>,
) {

    @OptIn(ExperimentalStdlibApi::class)
    fun call(args: Array<PyObject>, keywords: Array<String>): Any? {
        val signatures = functions.keys.flatMap { signature -> signature.parameters }.toSet()

        val argParser =
            PyArgParser.parseArgs(
                args,
                keywords,
                signatures.map { it.name }.toTypedArray(),
                signatures.map { it.type.javaType as Class<*> }.toTypedArray(),
                this.name,
            )

        functions.forEach { f ->
            val signature = f.key
            val function = f.value
            if (
                signature.parameters.all {
                    argParser.containsKey(it.name) || it.type.isMarkedNullable
                }
            ) {
                return function(
                    signature.parameters.associateBy(
                        { it.name },
                        {
                            val pyValue =
                                argParser.getPyObject(it.name).getOrNull()
                                    ?: return@associateBy null
                            val jValue = TypeUtilities.pyToJava(pyValue)
                            return@associateBy TypeUtilities.coerce(
                                jValue,
                                it.type.javaType as Class<*>,
                            )
                        },
                    )
                )
            }
        }

        val validSignatures =
            functions.keys.joinToString(", ") { functionSignature ->
                functionSignature.parameters.joinToString(", ", "(", ")") { it.name }
            }
        val message =
            "No matching function signature found for '$name'. Valid signatures include: $validSignatures"
        throw Py.TypeError(message)
    }
}
