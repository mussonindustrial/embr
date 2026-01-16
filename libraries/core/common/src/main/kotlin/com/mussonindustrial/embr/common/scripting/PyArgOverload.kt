package com.mussonindustrial.embr.common.scripting

import com.inductiveautomation.ignition.common.TypeUtilities
import com.inductiveautomation.ignition.common.script.PyArgParser
import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.KType
import kotlin.reflect.javaType
import org.python.core.Py
import org.python.core.PyObject

class PyArgOverload(
    val name: String,
    private val functions: Map<FunctionSignature, (args: Map<String, Any?>) -> Any?>,
) {

    fun call(args: Array<PyObject>, keywords: Array<String>): Any? {
        val signatures = functions.keys.flatMap { signature -> signature.parameters }.toSet()

        val argParser =
            PyArgParser.parseArgs(
                args,
                keywords,
                signatures.map { it.name }.toTypedArray(),
                signatures.map { it.type.rawJavaClass() }.toTypedArray(),
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
                                argParser.getPyObject(it.name).orElse(null)
                                    ?: return@associateBy null
                            val jValue = TypeUtilities.pyToJava(pyValue)
                            return@associateBy TypeUtilities.coerce(jValue, it.type.rawJavaClass())
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

    @OptIn(ExperimentalStdlibApi::class)
    private fun KType.rawJavaClass(): Class<*> {
        return extractClass(this.javaType)
    }

    private fun extractClass(type: Type): Class<*> =
        when (type) {
            is Class<*> -> type

            is ParameterizedType -> extractClass(type.rawType)

            is GenericArrayType -> {
                val component = extractClass(type.genericComponentType)
                java.lang.reflect.Array.newInstance(component, 0).javaClass
            }

            is WildcardType -> extractClass(type.upperBounds.first())

            else ->
                throw IllegalArgumentException("Unsupported type: $type (${type::class.java.name})")
        }
}
