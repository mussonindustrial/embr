package com.mussonindustrial.ignition.embr.periscope.scripting

import com.inductiveautomation.ignition.common.TypeUtilities
import com.inductiveautomation.ignition.common.script.builtin.KeywordArgs
import com.inductiveautomation.ignition.common.script.hints.ScriptFunction
import com.inductiveautomation.ignition.common.util.ExecutionQueue
import com.inductiveautomation.ignition.common.util.LogUtil
import com.inductiveautomation.perspective.gateway.api.PerspectiveContext
import com.inductiveautomation.perspective.gateway.api.PerspectiveElement
import com.inductiveautomation.perspective.gateway.script.AbstractScriptingFunctions
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

        if (delay == 0L) {
            this.submit { block }
        } else {
            executorService.schedule({ this.submit { block } }, delay, unit)
        }
    }

    private fun invokeLater(
        function: PyFunction,
        delay: Long,
        scope: String,
        sessionId: String?,
        pageId: String?,
    ) {

        val originalThreadContext = ThreadContext.get()
        val operator: (PerspectiveElement) -> Unit = { scope ->
            scope.session.queue().schedule(
                scope.session.perspectiveContext.scheduler,
                delay,
                TimeUnit.MILLISECONDS,
            ) {
                try {
                    if (!scope.isRunning) {
                        log.trace("Lifecycle object not running.")
                        return@schedule
                    }

                    withThreadContext(scope.threadContext) {
                        scope.session.scriptManager.runFunction(function)
                    }
                } catch (error: Exception) {
                    originalThreadContext.view.get()?.mdcSetup()
                    scope.session.sendErrorToDesigner(error.message, error)
                    scope.session.logger.error("Exception occurred on Perspective queue.", error)
                    originalThreadContext.view.get()?.mdcTeardown()
                    throw error
                }
            }
        }

        when (scope) {
            "view" -> operateOnView { operator }
            "page" -> operateOnPage(getPerspectiveArgumentMap(pageId, sessionId)) { operator }
            "session" -> operateOnSession(getPerspectiveArgumentMap(pageId, sessionId)) { operator }
            else -> throw IllegalArgumentException("Invalid scope \"$scope\".")
        }
    }

    inner class ScriptOverloads {
        val queueSubmit =
            PyArgOverloadBuilder<Unit>()
                .setName("invokeLater")
                .addOverload(
                    {
                        val function = it["function"] as PyFunction
                        val scope = it["scope"] as? String ?: "view"
                        val delay = TypeUtilities.toLong(it["delay"] ?: 0)
                        val sessionId = it["sessionId"] as? String
                        val pageId = it["pageId"] as? String
                        invokeLater(function, delay, scope, sessionId, pageId)
                    },
                    "function" to typeOf<PyFunction>(),
                    "scope" to typeOf<String?>(),
                    "delay" to typeOf<Long?>(),
                    "sessionId" to typeOf<String?>(),
                    "pageId" to typeOf<String?>(),
                )
                .build()
    }

    @ScriptFunction(docBundlePrefix = "${Meta.BUNDLE_PREFIX}.script")
    @KeywordArgs(
        names = ["function", "scope", "delay", "sessionId", "pageId"],
        types = [PyFunction::class, Long::class, String::class, String::class, String::class],
    )
    @Suppress("unused")
    fun invokeLater(args: Array<PyObject>, keywords: Array<String>) =
        overloads.queueSubmit.call(args, keywords)
}
