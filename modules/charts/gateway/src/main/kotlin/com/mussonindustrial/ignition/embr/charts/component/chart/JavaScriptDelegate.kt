package com.mussonindustrial.ignition.embr.charts.component.chart

import com.inductiveautomation.ignition.common.TypeUtilities
import com.inductiveautomation.ignition.common.gson.JsonElement
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.script.builtin.KeywordArgs
import com.inductiveautomation.perspective.gateway.api.Component
import com.inductiveautomation.perspective.gateway.api.ComponentModelDelegate
import com.inductiveautomation.perspective.gateway.api.ScriptCallable
import com.inductiveautomation.perspective.gateway.messages.EventFiredMsg
import com.mussonindustrial.embr.common.scripting.PyArgOverloadBuilder
import com.mussonindustrial.ignition.embr.periscope.exception.JavaScriptException
import com.mussonindustrial.ignition.embr.periscope.exception.JavaScriptExecutionException
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.reflect.typeOf
import org.python.core.PyDictionary
import org.python.core.PyFunction
import org.python.core.PyObject

class JavaScriptDelegate(val component: Component, val delegate: ComponentModelDelegate) :
    JavaScriptProxy {

    private val queue = component.session.queue()
    private val requestsInProgress = ConcurrentHashMap<String, CompletableFuture<PyObject?>>()
    private val overloads = ScriptOverloads()
    private val timeout = 30000L

    companion object {
        const val MESSAGE_RUN = "js-run"
        const val MESSAGE_RESOLVE = "js-resolve"
        const val MESSAGE_ERROR = "js-error"

        private val canHandle = setOf(MESSAGE_RESOLVE, MESSAGE_ERROR)
    }

    fun handles(message: EventFiredMsg): Boolean {
        return canHandle.contains(message.eventName)
    }

    private fun onJavaScriptResolve(message: JavaScriptResolveMsg) {
        requestsInProgress[message.id]?.complete(message.getValue())
    }

    private fun onJavaScriptError(message: JavaScriptErrorMsg) {
        requestsInProgress[message.id]?.completeExceptionally(message.getError())
    }

    fun handleEvent(message: EventFiredMsg) {
        when (message.eventName) {
            MESSAGE_RESOLVE -> {
                onJavaScriptResolve(JavaScriptResolveMsg(message.event))
            }
            MESSAGE_ERROR -> {
                onJavaScriptError(JavaScriptErrorMsg(message.event))
            }
        }
    }

    private fun runJavaScript(
        function: String,
        args: PyDictionary?,
        callback: PyFunction? = null,
    ): CompletableFuture<PyObject?> {

        val future = CompletableFuture<PyObject?>()
        future.orTimeout(this.timeout, TimeUnit.SECONDS)

        val id = UUID.randomUUID().toString()
        requestsInProgress[id] = future

        future.whenCompleteAsync(
            { result, error ->
                requestsInProgress.remove(id)

                if (result != null && callback != null) {
                    component.session.scriptManager.runFunction(callback, result)
                }

                if (error != null) {
                    component.session.sendErrorToDesigner(error.message, error)
                    throw error
                }
            },
            queue::submit
        )

        delegate.fireEvent(MESSAGE_RUN, JavaScriptRunMsg(id, function, args).getPayload())
        return future
    }

    @ScriptCallable
    @KeywordArgs(
        names = ["function", "args"],
        types = [String::class, PyDictionary::class, String::class, String::class],
    )
    @Suppress("unused")
    override fun runJavaScriptBlocking(args: Array<PyObject>, keywords: Array<String>): Any? {
        return overloads.runJavaScriptBlocking.call(args, keywords)
    }

    @ScriptCallable
    @KeywordArgs(
        names = ["function", "args", "callback"],
        types =
            [String::class, PyDictionary::class, PyFunction::class, String::class, String::class],
    )
    @Suppress("unused")
    override fun runJavaScriptAsync(args: Array<PyObject>, keywords: Array<String>) {
        overloads.runJavaScriptAsync.call(args, keywords)
    }

    inner class ScriptOverloads {
        val runJavaScriptAsync =
            PyArgOverloadBuilder()
                .setName("runJavaScriptAsync")
                .addOverload(
                    {
                        val function = TypeUtilities.toString(it[0])!!
                        val args = it[1] as? PyDictionary
                        val callback = it[2] as? PyFunction
                        runJavaScript(function, args, callback)
                        null
                    },
                    "function" to typeOf<String>(),
                    "args" to typeOf<PyDictionary?>(),
                    "callback" to typeOf<PyFunction?>(),
                )
                .build()

        val runJavaScriptBlocking =
            PyArgOverloadBuilder()
                .setName("runJavaScriptBlocking")
                .addOverload(
                    {
                        val function = TypeUtilities.toString(it[0])!!
                        val args = it[1] as? PyDictionary
                        runJavaScript(function, args, null).get(timeout, TimeUnit.SECONDS)
                    },
                    "function" to typeOf<String>(),
                    "args" to typeOf<PyDictionary?>(),
                )
                .build()
    }

    class JavaScriptRunMsg(
        private val id: String,
        private val function: String,
        private val args: PyDictionary?
    ) {
        fun getPayload(): JsonObject {
            return JsonObject().apply {
                addProperty("id", id)
                addProperty("function", function)
                add("args", TypeUtilities.pyToGson(args))
            }
        }
    }

    class JavaScriptErrorMsg(event: JsonObject) {
        val id: String = event.get("id").asString
        val error: JsonElement = event.get("error")

        fun getError(): Throwable {
            val commonMessage = "Error running client-side JavaScript."
            if (error.isJsonObject) {

                val name = error.asJsonObject.get("name")?.asString ?: "<no error.name>"
                val message = error.asJsonObject.get("message")?.asString ?: "<no error.message>"
                val stack =
                    error.asJsonObject.get("stack")?.asString ?: "No JavaScript stack available"
                val cause = JavaScriptException("${name}: $message", JavaScriptException(stack))

                return JavaScriptExecutionException(commonMessage, cause)
            }

            return JavaScriptExecutionException("${commonMessage}: ${error.asString}")
        }
    }

    class JavaScriptResolveMsg(event: JsonObject) {
        val id: String = event.get("id").asString
        val data: JsonElement? = if (event.has("data")) event.get("data") else null

        fun getValue(): PyObject? {
            return TypeUtilities.gsonToPy(data)
        }
    }
}
