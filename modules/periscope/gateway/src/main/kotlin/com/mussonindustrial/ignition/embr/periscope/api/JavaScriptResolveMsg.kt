package com.mussonindustrial.ignition.embr.periscope.api

import com.inductiveautomation.ignition.common.TypeUtilities
import com.inductiveautomation.ignition.common.gson.JsonElement
import org.python.core.PyObject

class JavaScriptResolveMsg {
    companion object {
        const val PROTOCOL: String = "periscope-js-resolve"
    }

    lateinit var id: String
    lateinit var data: JsonElement

    fun getValue(): PyObject? {
        return TypeUtilities.gsonToPy(data)
    }
}
