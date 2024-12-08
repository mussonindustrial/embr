package com.mussonindustrial.ignition.embr.periscope.api

import com.inductiveautomation.ignition.common.TypeUtilities
import com.inductiveautomation.ignition.common.gson.JsonObject
import org.python.core.PyDictionary

class JavaScriptRunMsg(
    private val id: String,
    private val function: String,
    private val args: PyDictionary?
) {
    companion object {
        const val PROTOCOL: String = "periscope-js-run"
    }

    fun getPayload(): JsonObject {
        return JsonObject().apply {
            addProperty("id", id)
            addProperty("function", function)
            add("args", TypeUtilities.pyToGson(args))
        }
    }
}
