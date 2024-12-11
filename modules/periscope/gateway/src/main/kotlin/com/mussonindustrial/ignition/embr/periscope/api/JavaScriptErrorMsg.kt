package com.mussonindustrial.ignition.embr.periscope.api

import com.inductiveautomation.ignition.common.gson.JsonElement
import com.mussonindustrial.embr.perspective.common.exceptions.JavaScriptException
import com.mussonindustrial.embr.perspective.common.exceptions.JavaScriptExecutionException

class JavaScriptErrorMsg {
    companion object {
        const val PROTOCOL: String = "periscope-js-error"
    }

    lateinit var id: String
    lateinit var error: JsonElement

    fun getError(): Throwable {
        val commonMessage = "Error running client-side JavaScript."
        if (error.isJsonObject) {

            val name = error.asJsonObject.get("name")?.asString ?: "<no error.name>"
            val message = error.asJsonObject.get("message")?.asString ?: "<no error.message>"
            val stack = error.asJsonObject.get("stack")?.asString ?: "No JavaScript stack available"
            val cause = JavaScriptException("${name}: $message", JavaScriptException(stack))

            return JavaScriptExecutionException(commonMessage, cause)
        }

        return JavaScriptExecutionException("${commonMessage}: ${error.asString}")
    }
}
