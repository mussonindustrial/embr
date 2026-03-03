package com.mussonindustrial.ignition.embr.periscope.scripting

import com.inductiveautomation.ignition.common.TypeUtilities
import com.inductiveautomation.ignition.common.script.builtin.KeywordArgs
import com.inductiveautomation.ignition.common.script.hints.ScriptFunction
import com.inductiveautomation.ignition.common.util.ExecutionQueue
import com.inductiveautomation.ignition.common.util.LogUtil
import com.inductiveautomation.perspective.gateway.api.Page
import com.inductiveautomation.perspective.gateway.api.PerspectiveContext
import com.inductiveautomation.perspective.gateway.api.PerspectiveElement
import com.inductiveautomation.perspective.gateway.api.Session
import com.inductiveautomation.perspective.gateway.model.ViewModel
import com.inductiveautomation.perspective.gateway.script.AbstractScriptingFunctions
import com.inductiveautomation.perspective.gateway.script.PageScriptWrapper
import com.inductiveautomation.perspective.gateway.script.PropertyTreeOwnerScriptWrapper
import com.inductiveautomation.perspective.gateway.script.SessionScriptWrapper
import com.inductiveautomation.perspective.gateway.script.ViewModelScriptWrapper
import com.mussonindustrial.embr.common.scripting.PyArgOverloadBuilder
import com.mussonindustrial.embr.perspective.gateway.model.ThreadContext
import com.mussonindustrial.embr.perspective.gateway.model.getPerspectiveArgumentMap
import com.mussonindustrial.embr.perspective.gateway.model.threadContext
import com.mussonindustrial.embr.perspective.gateway.model.withThreadContext
import com.mussonindustrial.ignition.embr.periscope.Meta
import com.mussonindustrial.ignition.embr.periscope.PeriscopeGatewayContext
import java.util.concurrent.*
import kotlin.reflect.typeOf
import org.python.core.PyFunction
import org.python.core.PyObject

class QueueFunctions(private val context: PeriscopeGatewayContext) : AbstractScriptingFunctions() {

    private val log = LogUtil.getModuleLogger(Meta.SHORT_MODULE_ID, "QueueFunctions")
    private val overloads = ScriptOverloads()

    override fun getContext(): PerspectiveContext {
        return context.perspectiveContext
    }

    private fun ExecutionQueue.schedule(
        executorService: ScheduledExecutorService,
        delay: Long,
        unit: TimeUnit,
        block: () -> Unit,
    ) {
        executorService.schedule({ this.submit(block) }, delay, unit)
    }

    private fun invokeOnQueue(
        function: PyFunction,
        delay: Long,
        scope: String,
        sessionId: String?,
        pageId: String?,
    ) {

        val originalThreadContext = ThreadContext.get()
        val operation: (PerspectiveElement) -> Unit = { element ->
            element.session.queue().schedule(
                element.session.perspectiveContext.scheduler,
                delay,
                TimeUnit.MILLISECONDS,
            ) {
                try {
                    if (!element.isRunning) {
                        log.trace("Lifecycle object not running: ${element.name}")
                        return@schedule
                    }

                    val wrappedElement =
                        when (element) {
                            is ViewModel -> ViewModelScriptWrapper(element)
                            is Page -> PageScriptWrapper(element)
                            is Session -> SessionScriptWrapper(element)
                            else -> PropertyTreeOwnerScriptWrapper(element)
                        }

                    withThreadContext(element.threadContext) {
                        element.session.scriptManager.runFunction(function, wrappedElement)
                    }
                } catch (error: Exception) {
                    originalThreadContext.view.get()?.mdcSetup()
                    element.session.sendErrorToDesigner(error.message, error)
                    element.session.logger.error("Exception occurred on Perspective queue.", error)
                    originalThreadContext.view.get()?.mdcTeardown()
                    throw error
                }
            }
        }

        when (scope) {
            "view" -> operateOnView(operation)
            "page" -> operateOnPage(getPerspectiveArgumentMap(pageId, sessionId), operation)
            "session" -> operateOnSession(getPerspectiveArgumentMap(pageId, sessionId), operation)
            else -> throw IllegalArgumentException("Invalid scope \"$scope\".")
        }
    }

    inner class ScriptOverloads {
        val queueSubmit =
            PyArgOverloadBuilder<Unit>()
                .setName("invokeOnQueue")
                .addOverload(
                    {
                        val function = it["function"] as PyFunction
                        val delay = TypeUtilities.toLong(it["delay"] ?: 0)
                        val scope = it["scope"] as? String ?: "view"
                        val sessionId = it["sessionId"] as? String
                        val pageId = it["pageId"] as? String
                        invokeOnQueue(function, delay, scope, sessionId, pageId)
                    },
                    "function" to typeOf<PyFunction>(),
                    "delay" to typeOf<Long?>(),
                    "scope" to typeOf<String?>(),
                    "sessionId" to typeOf<String?>(),
                    "pageId" to typeOf<String?>(),
                )
                .build()
    }

    @ScriptFunction(docBundlePrefix = "${Meta.BUNDLE_PREFIX}.script")
    @KeywordArgs(
        names = ["function", "delay", "scope", "sessionId", "pageId"],
        types = [PyFunction::class, Long::class, String::class, String::class, String::class],
    )
    @Suppress("unused")
    fun invokeOnQueue(args: Array<PyObject>, keywords: Array<String>) =
        overloads.queueSubmit.call(args, keywords)
}
