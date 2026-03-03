package com.mussonindustrial.embr.perspective.gateway.model

import com.inductiveautomation.ignition.common.script.builtin.PyArgumentMap
import org.python.core.PyString

fun getPerspectiveArgumentMap(pageId: String?, sessionId: String?): PyArgumentMap {

    val args = mapOf("pageId" to pageId, "sessionId" to sessionId)

    return PyArgumentMap.interpretPyArgs(
        args.mapNotNull { arg -> arg.value?.let { PyString(it) } }.toTypedArray(),
        args.mapNotNull { arg -> arg.value?.let { arg.key } }.toTypedArray(),
        arrayOf("pageId", "sessionId"),
        arrayOf(String::class.java, String::class.java),
    )
}
