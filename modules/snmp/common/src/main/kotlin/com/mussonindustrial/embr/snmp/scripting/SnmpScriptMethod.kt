package com.mussonindustrial.embr.snmp.scripting

import com.mussonindustrial.embr.common.scripting.PyArgOverload
import com.mussonindustrial.embr.common.scripting.PyArgOverloadBuilder

class SnmpScriptMethod<T>(val name: String, val isWrite: Boolean, val overload: PyArgOverload<T>) {

    companion object {
        fun <T> of(
            name: String,
            isWrite: Boolean,
            build: PyArgOverloadBuilder<T>.() -> Unit,
        ): SnmpScriptMethod<T> =
            SnmpScriptMethod(
                name = name,
                isWrite = isWrite,
                overload = PyArgOverloadBuilder<T>().setName(name).apply(build).build(),
            )
    }
}
