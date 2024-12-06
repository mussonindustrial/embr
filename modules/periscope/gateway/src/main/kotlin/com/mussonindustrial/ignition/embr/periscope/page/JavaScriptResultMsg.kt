package com.mussonindustrial.ignition.embr.periscope.page

import com.inductiveautomation.ignition.common.TypeUtilities
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.mussonindustrial.ignition.embr.periscope.exception.JavaScriptException
import org.python.core.PyObject

class JavaScriptResultMsg {
    companion object {
        const val PROTOCOL: String = "periscope-runJavaScript-result"
    }

    lateinit var id: String
    lateinit var result: JsonObject

    fun getError(): Throwable? {
        if (!result.get("success").asBoolean) {
            val errorMessage = result.get("value").asString
            return JavaScriptException(errorMessage)
        }
        return null
    }

    fun getValue(): PyObject? {
        return TypeUtilities.gsonToPy(result.get("value"))
    }
}
