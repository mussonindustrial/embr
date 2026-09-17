package com.mussonindustrial.embr.perspective.gateway.model

import com.inductiveautomation.ignition.common.script.builtin.PyArgumentMap
import com.inductiveautomation.perspective.gateway.model.PageModel
import com.inductiveautomation.perspective.gateway.script.AbstractScriptingFunctions
import com.inductiveautomation.perspective.gateway.session.InternalSession
import org.python.core.PyString

fun getPerspectiveArgumentMap(pageId: String?, sessionId: String?): PyArgumentMap {
    val args = mapOf("pageId" to pageId, "sessionId" to sessionId)

    return PyArgumentMap.interpretPyArgs(
        args.mapNotNull { (_, value) -> value?.let { PyString(it) } }.toTypedArray(),
        args.mapNotNull { (key, value) -> value?.let { key } }.toTypedArray(),
        arrayOf("pageId", "sessionId"),
        arrayOf(String::class.java, String::class.java),
    )
}

abstract class PerspectiveScriptingFunctions : AbstractScriptingFunctions() {

    private class Result<T>(val value: T)

    protected fun <T> onPage(
        pageId: String?,
        sessionId: String?,
        operation: (PageModel) -> T,
    ): T {
        val argumentMap = getPerspectiveArgumentMap(pageId, sessionId)
        var result: Result<T>? = null

        operateOnPage(argumentMap) {
            result = Result(operation(it))
        }

        return checkNotNull(result).value
    }

    protected fun <T> onSession(
        sessionId: String?,
        operation: (InternalSession) -> T,
    ): T {
        val argumentMap = getPerspectiveArgumentMap(null, sessionId)
        var result: Result<T>? = null

        operateOnSession(argumentMap) {
            result = Result(operation(it))
        }

        @Suppress("UNCHECKED_CAST")
        return checkNotNull(result).value
    }
}
