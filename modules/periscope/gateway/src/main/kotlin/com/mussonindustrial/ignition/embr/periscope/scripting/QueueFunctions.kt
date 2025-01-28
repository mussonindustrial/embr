package com.mussonindustrial.ignition.embr.periscope.scripting

import com.inductiveautomation.ignition.common.TypeUtilities
import com.inductiveautomation.ignition.common.script.builtin.KeywordArgs
import com.inductiveautomation.ignition.common.script.hints.ScriptFunction
import com.inductiveautomation.ignition.common.util.LogUtil
import com.inductiveautomation.perspective.gateway.api.PerspectiveContext
import com.inductiveautomation.perspective.gateway.api.PerspectiveElement
import com.inductiveautomation.perspective.gateway.model.PageModel
import com.inductiveautomation.perspective.gateway.model.ViewModel
import com.inductiveautomation.perspective.gateway.script.AbstractScriptingFunctions
import com.inductiveautomation.perspective.gateway.session.InternalSession
import com.mussonindustrial.embr.common.scripting.PyArgOverloadBuilder
import com.mussonindustrial.embr.perspective.gateway.model.ThreadContext
import com.mussonindustrial.embr.perspective.gateway.model.withThreadContext
import com.mussonindustrial.ignition.embr.periscope.Meta
import com.mussonindustrial.ignition.embr.periscope.PeriscopeGatewayContext
import com.mussonindustrial.ignition.embr.periscope.api.ExecutionQueueScheduledRunnable
import com.mussonindustrial.ignition.embr.periscope.api.schedule
import com.mussonindustrial.ignition.embr.periscope.model.PerspectiveExecutionContext
import java.util.WeakHashMap
import java.util.concurrent.*
import kotlin.reflect.typeOf
import org.python.core.PyFunction
import org.python.core.PyObject

class QueueFunctions(private val context: PeriscopeGatewayContext) : AbstractScriptingFunctions() {

    private val log = LogUtil.getModuleLogger(Meta.SHORT_MODULE_ID, "QueueFunctions")
    private val overloads = ScriptOverloads()
    private val scopes =
        WeakHashMap<
            PerspectiveElement, ConcurrentHashMap<String, ExecutionQueueScheduledRunnable>
        >()

    override fun getContext(): PerspectiveContext {
        return context.perspectiveContext
    }

    private fun getScheduled(
        scope: PerspectiveElement
    ): ConcurrentHashMap<String, ExecutionQueueScheduledRunnable> {
        return scopes.getOrPut(scope) { ConcurrentHashMap() }
    }

    private fun getScope(
        executionContext: PerspectiveExecutionContext,
        scope: String
    ): PerspectiveElement? {
        when (scope) {
            "view" -> {
                return executionContext.getView()
            }
            "page" -> {
                return executionContext.getPage()
            }
            "session" -> {
                return executionContext.getSession()
            }
            else -> {
                throw IllegalArgumentException(
                    "Unsupported scope $scope. Valid scopes: ['view', 'page', 'session']"
                )
            }
        }
    }

    private fun queueSubmit(
        function: PyFunction,
        delay: Long,
        key: String?,
        scope: String,
        sessionId: String?,
        pageId: String?,
    ): ExecutionQueueScheduledRunnable? {

        val executionContext =
            PerspectiveExecutionContext(context.perspectiveContext, pageId, sessionId)

        val scopeElement = getScope(executionContext, scope)
        require(scopeElement != null) { "Failed to acquire scope \"$scope\"." }

        if (!scopeElement.isRunning) {
            log.debug("Scope element is not running.")
            return null
        }

        val session = scopeElement.session

        val queue = session.queue()
        val scheduler = session.perspectiveContext.scheduler
        val scriptManager = session.scriptManager
        val originalThreadContext = ThreadContext.get()
        val scopeThreadContext =
            ThreadContext(
                scopeElement.view as? ViewModel,
                scopeElement.page as? PageModel,
                scopeElement.session as? InternalSession
            )

        val scheduled = getScheduled(scopeElement)
        scheduled[key]?.cancel()

        val runnable =
            queue.schedule(
                scheduler,
                {
                    try {
                        scheduled.remove(key)
                        withThreadContext(scopeThreadContext) {
                            scriptManager.runFunction(function)
                        }
                    } catch (error: IllegalStateException) {
                        log.trace("Lifecycle object closed.", error)
                    } catch (error: Exception) {
                        originalThreadContext.view.get()?.mdcSetup()
                        scopeElement.session.sendErrorToDesigner(error.message, error)
                        scopeElement.session.logger.error(
                            "Exception occurred on Perspective queue.",
                            error
                        )
                        originalThreadContext.view.get()?.mdcTeardown()
                        throw error
                    }
                },
                delay,
                TimeUnit.MILLISECONDS
            )

        if (key !== null) {
            scheduled[key] = runnable
        }

        return runnable
    }

    private fun queueCancel(
        key: String,
        scope: String,
        sessionId: String?,
        pageId: String?,
    ) {
        val executionContext =
            PerspectiveExecutionContext(context.perspectiveContext, pageId, sessionId)

        val scopeElement = getScope(executionContext, scope)
        require(scopeElement != null) { "Failed to acquire scope \"$scope\"." }

        if (!scopeElement.isRunning) {
            return
        }

        val queue = scopeElement.session.queue()
        val scheduled = getScheduled(scopeElement)

        queue.submit {
            scheduled[key]?.cancel()
            scheduled.remove(key)
        }
    }

    inner class ScriptOverloads {
        val queueSubmit =
            PyArgOverloadBuilder()
                .setName("queueSubmit")
                .addOverload(
                    {
                        val function = it["function"] as PyFunction
                        val delay = TypeUtilities.toLong(it["delay"] ?: 0)
                        val key = it["key"] as? String
                        val scope = it["scope"] as? String ?: "view"
                        val sessionId = it["sessionId"] as? String
                        val pageId = it["pageId"] as? String
                        queueSubmit(function, delay, key, scope, sessionId, pageId)
                        null
                    },
                    "function" to typeOf<PyFunction>(),
                    "delay" to typeOf<Long?>(),
                    "key" to typeOf<String?>(),
                    "scope" to typeOf<String?>(),
                    "sessionId" to typeOf<String?>(),
                    "pageId" to typeOf<String?>(),
                )
                .build()

        val queueCancel =
            PyArgOverloadBuilder()
                .setName("queueCancel")
                .addOverload(
                    {
                        val key = it["key"] as String
                        val scope = it["scope"] as? String ?: "view"
                        val sessionId = it["sessionId"] as? String
                        val pageId = it["pageId"] as? String
                        queueCancel(key, scope, sessionId, pageId)
                        null
                    },
                    "key" to typeOf<String>(),
                    "scope" to typeOf<String?>(),
                    "sessionId" to typeOf<String?>(),
                    "pageId" to typeOf<String?>(),
                )
                .build()
    }

    @ScriptFunction(docBundlePrefix = "${Meta.BUNDLE_PREFIX}.script")
    @KeywordArgs(
        names = ["function", "delay", "key", "scope", "sessionId", "pageId"],
        types =
            [
                PyFunction::class,
                Long::class,
                String::class,
                String::class,
                String::class,
                String::class
            ],
    )
    @Suppress("unused")
    fun queueSubmit(args: Array<PyObject>, keywords: Array<String>) =
        overloads.queueSubmit.call(args, keywords)

    @ScriptFunction(docBundlePrefix = "${Meta.BUNDLE_PREFIX}.script")
    @KeywordArgs(
        names = ["key", "scope", "sessionId", "pageId"],
        types = [String::class, String::class, String::class, String::class],
    )
    @Suppress("unused")
    fun queueCancel(args: Array<PyObject>, keywords: Array<String>) {
        overloads.queueCancel.call(args, keywords)
    }
}
