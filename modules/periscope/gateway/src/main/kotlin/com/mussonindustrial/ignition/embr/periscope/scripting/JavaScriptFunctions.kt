package com.mussonindustrial.ignition.embr.periscope.scripting

import com.inductiveautomation.ignition.common.TypeUtilities
import com.inductiveautomation.ignition.common.script.builtin.KeywordArgs
import com.inductiveautomation.ignition.common.script.builtin.PyArgumentMap
import com.inductiveautomation.ignition.common.script.hints.ScriptFunction
import com.inductiveautomation.ignition.common.util.ExecutionQueue
import com.inductiveautomation.ignition.common.util.LogUtil
import com.inductiveautomation.perspective.gateway.api.PerspectiveContext
import com.inductiveautomation.perspective.gateway.model.MessageChannel
import com.inductiveautomation.perspective.gateway.model.PageModel
import com.inductiveautomation.perspective.gateway.script.AbstractScriptingFunctions
import com.mussonindustrial.embr.common.scripting.PyArgOverloadBuilder
import com.mussonindustrial.embr.perspective.gateway.model.ThreadContext
import com.mussonindustrial.embr.perspective.gateway.model.withThreadContext
import com.mussonindustrial.embr.perspective.gateway.reflect.getHandlers
import com.mussonindustrial.ignition.embr.periscope.Meta
import com.mussonindustrial.ignition.embr.periscope.PeriscopeGatewayContext
import com.mussonindustrial.ignition.embr.periscope.api.JavaScriptErrorMsg
import com.mussonindustrial.ignition.embr.periscope.api.JavaScriptResolveMsg
import com.mussonindustrial.ignition.embr.periscope.api.JavaScriptRunMsg
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.reflect.typeOf
import org.python.core.PyDictionary
import org.python.core.PyFunction
import org.python.core.PyObject
import org.python.core.PyString

class JavaScriptFunctions(private val context: PeriscopeGatewayContext) :
    AbstractScriptingFunctions() {

    private val log = LogUtil.getModuleLogger(Meta.SHORT_MODULE_ID, "JavaScriptFunctions")
    private val requestsInProgress = ConcurrentHashMap<String, CompletableFuture<PyObject?>>()
    private val overloads = ScriptOverloads()
    private val timeout = 30L

    override fun getContext(): PerspectiveContext {
        return context.perspectiveContext
    }

    private fun getPerspectiveArgumentMap(pageId: String?, sessionId: String?): PyArgumentMap {

        val args = mapOf("pageId" to pageId, "sessionId" to sessionId)

        return PyArgumentMap.interpretPyArgs(
            args.mapNotNull { arg -> arg.value?.let { PyString(it) } }.toTypedArray(),
            args.mapNotNull { arg -> arg.value?.let { arg.key } }.toTypedArray(),
            arrayOf("pageId", "sessionId"),
            arrayOf(String::class.java, String::class.java)
        )
    }

    private fun onJavaScriptResolve(messageChannel: MessageChannel, message: JavaScriptResolveMsg) {
        requestsInProgress[message.id]?.complete(message.getValue())
    }

    private fun onJavaScriptError(messageChannel: MessageChannel, message: JavaScriptErrorMsg) {
        requestsInProgress[message.id]?.completeExceptionally(message.getError())
    }

    private fun registerHandlers(page: PageModel) {
        page.getHandlers().apply {
            register(
                JavaScriptResolveMsg.PROTOCOL,
                this@JavaScriptFunctions::onJavaScriptResolve,
                JavaScriptResolveMsg::class.java
            )
            register(
                JavaScriptErrorMsg.PROTOCOL,
                this@JavaScriptFunctions::onJavaScriptError,
                JavaScriptErrorMsg::class.java
            )
        }
    }

    private fun runJavaScript(
        function: String,
        args: PyDictionary?,
        callback: PyFunction? = null,
        sessionId: String? = null,
        pageId: String? = null,
    ): CompletableFuture<PyObject?> {

        val future = CompletableFuture<PyObject?>()
        future.orTimeout(this.timeout, TimeUnit.SECONDS)

        this.operateOnPage(getPerspectiveArgumentMap(pageId, sessionId)) { page ->
            val originalThreadContext = ThreadContext.get()
            val queue: ExecutionQueue = page.session.queue()

            registerHandlers(page)

            val id = UUID.randomUUID().toString()
            val message =
                JavaScriptRunMsg(
                    id,
                    function,
                    args,
                    originalThreadContext,
                )
            requestsInProgress[id] = future

            future
                .thenAcceptAsync(
                    { result ->
                        if (callback != null) {
                            withThreadContext(originalThreadContext) {
                                page.session.scriptManager.runFunction(callback, result)
                            }
                        }
                    },
                    queue::submit
                )
                .exceptionally { error ->
                    originalThreadContext.view.get()?.mdcSetup()
                    page.session.sendErrorToDesigner(error.message, error)
                    page.log.error("Exception occurred executing client-side JavaScript.", error)
                    originalThreadContext.view.get()?.mdcTeardown()
                    throw error
                }
                .whenCompleteAsync({ _, _ -> requestsInProgress.remove(id) }, queue::submit)

            page.send(JavaScriptRunMsg.PROTOCOL, message.getPayload())
        }

        return future
    }

    inner class ScriptOverloads {
        val runJavaScriptAsync =
            PyArgOverloadBuilder()
                .setName("runJavaScriptAsync")
                .addOverload(
                    {
                        val function = TypeUtilities.toString(it["function"])!!
                        val args = it["args"] as? PyDictionary
                        val callback = it["callback"] as? PyFunction
                        val sessionId = it["sessionId"] as? String
                        val pageId = it["pageId"] as? String
                        runJavaScript(function, args, callback, sessionId, pageId)
                        null
                    },
                    "function" to typeOf<String>(),
                    "args" to typeOf<PyDictionary?>(),
                    "callback" to typeOf<PyFunction?>(),
                    "sessionId" to typeOf<String?>(),
                    "pageId" to typeOf<String?>(),
                )
                .build()

        val runJavaScriptBlocking =
            PyArgOverloadBuilder()
                .setName("runJavaScriptBlocking")
                .addOverload(
                    {
                        val function = TypeUtilities.toString(it["function"])!!
                        val args = it["args"] as? PyDictionary
                        val sessionId = it["sessionId"] as? String
                        val pageId = it["pageId"] as? String
                        runJavaScript(function, args, null, sessionId, pageId)
                            .get(timeout, TimeUnit.SECONDS)
                    },
                    "function" to typeOf<String>(),
                    "args" to typeOf<PyDictionary?>(),
                    "sessionId" to typeOf<String?>(),
                    "pageId" to typeOf<String?>(),
                )
                .build()
    }

    @ScriptFunction(docBundlePrefix = "${Meta.BUNDLE_PREFIX}.script")
    @KeywordArgs(
        names = ["function", "args", "sessionId", "pageId"],
        types = [String::class, PyDictionary::class, String::class, String::class],
    )
    @Suppress("unused")
    fun runJavaScriptBlocking(args: Array<PyObject>, keywords: Array<String>) =
        overloads.runJavaScriptBlocking.call(args, keywords)

    @ScriptFunction(docBundlePrefix = "${Meta.BUNDLE_PREFIX}.script")
    @KeywordArgs(
        names = ["function", "args", "callback", "sessionId", "pageId"],
        types =
            [String::class, PyDictionary::class, PyFunction::class, String::class, String::class],
    )
    @Suppress("unused")
    fun runJavaScriptAsync(args: Array<PyObject>, keywords: Array<String>) =
        overloads.runJavaScriptAsync.call(args, keywords)
}
