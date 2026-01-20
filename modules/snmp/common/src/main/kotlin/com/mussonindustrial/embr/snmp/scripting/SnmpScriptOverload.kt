package com.mussonindustrial.embr.snmp.scripting

import com.mussonindustrial.embr.common.scripting.PyArgOverload
import com.mussonindustrial.embr.common.scripting.PyArgOverloadBuilder
import org.python.core.PyObject

data class SnmpScriptOverload<T>(
    val name: String,
    val isWrite: Boolean,
    val overload: PyArgOverload,
) {

    companion object {
        fun <T> of(
            name: String,
            isWrite: Boolean,
            build: PyArgOverloadBuilder.() -> Unit,
        ): SnmpScriptOverload<T> =
            SnmpScriptOverload(
                name = name,
                isWrite = isWrite,
                overload = PyArgOverloadBuilder().setName(name).apply(build).build(),
            )
    }

    @Suppress("UNCHECKED_CAST")
    fun call(args: Array<PyObject>, keywords: Array<String>): T {
        return overload.call(args, keywords) as T
    }
}
