package com.mussonindustrial.ignition.embr.periscope.scripting

import com.inductiveautomation.ignition.common.TypeUtilities
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.script.builtin.KeywordArgs
import com.inductiveautomation.ignition.common.script.hints.ScriptFunction
import com.inductiveautomation.perspective.gateway.model.MessageChannel
import com.inductiveautomation.perspective.gateway.model.PageModel
import com.mussonindustrial.embr.common.scripting.PyArgOverloadBuilder
import com.mussonindustrial.embr.perspective.gateway.reflect.getHandlers
import com.mussonindustrial.ignition.embr.periscope.Meta
import com.mussonindustrial.ignition.embr.periscope.page.JavaScriptResultMsg
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.reflect.typeOf
import org.python.core.PyDictionary
import org.python.core.PyFunction
import org.python.core.PyObject

class PerspectiveExtensionsImpl : PerspectiveExtensions {

    private val waitingRequests = WeakHashMap<String, CompletableFuture<PyObject?>>()
    private val overloads = ScriptOverloads()

    private fun requireThreadPage(): PageModel {
        val page =
            PageModel.PAGE.get()
                ?: throw IllegalAccessException("Must be called in a Perspective Page context")
        return page
    }

    fun onJavaScriptResult(messageChannel: MessageChannel, message: JavaScriptResultMsg) {
        waitingRequests.remove(message.id)?.let { future ->
            message.getError()?.let { future.completeExceptionally(it) }
                ?: future.complete(message.getValue())
        }
    }

    private fun doRunJavaScriptAsync(
        function: String,
        args: PyDictionary?,
        callback: PyFunction?
    ): CompletableFuture<PyObject?> {
        val page = requireThreadPage()
        val future = CompletableFuture<PyObject?>()
        val id = UUID.randomUUID().toString()

        page
            .getHandlers()
            .register(
                JavaScriptResultMsg.PROTOCOL,
                this::onJavaScriptResult,
                JavaScriptResultMsg::class.java
            )

        waitingRequests[id] = future

        page.send(
            ExtensionProtocols.RUN_JAVASCRIPT.protocol,
            JsonObject().apply {
                addProperty("id", id)
                addProperty("function", function)
                add("args", TypeUtilities.pyToGson(args))
            }
        )

        future.orTimeout(10000, TimeUnit.MILLISECONDS)

        future.whenComplete { result, _ ->
            callback?.let {
                page.session.queue().submit { page.session.scriptManager.runFunction(it, result) }
            }
        }

        return future
    }

    private fun doRunJavaScriptBlocking(function: String, args: PyDictionary?): PyObject? {
        return doRunJavaScriptAsync(function, args, null).join()
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
                        doRunJavaScriptAsync(function, args, callback)
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
                        doRunJavaScriptBlocking(function, args)
                    },
                    "function" to typeOf<String>(),
                    "args" to typeOf<PyDictionary?>(),
                )
                .build()
    }

    enum class ExtensionProtocols(val protocol: String) {
        RUN_JAVASCRIPT("periscope-runJavaScript"),
    }

    @KeywordArgs(
        names = ["function", "args"],
        types = [String::class, PyDictionary::class],
    )
    @Suppress("unused")
    @ScriptFunction(docBundlePrefix = "${Meta.BUNDLE_PREFIX}.script")
    override fun runJavaScriptBlocking(args: Array<PyObject>, keywords: Array<String>): Any? {
        return overloads.runJavaScriptBlocking.call(args, keywords)
    }

    @KeywordArgs(
        names = ["function", "args", "callback"],
        types = [String::class, PyDictionary::class, PyFunction::class],
    )
    @Suppress("unused")
    @ScriptFunction(docBundlePrefix = "${Meta.BUNDLE_PREFIX}.script")
    override fun runJavaScriptAsync(args: Array<PyObject>, keywords: Array<String>) {
        overloads.runJavaScriptAsync.call(args, keywords)
    }
}
