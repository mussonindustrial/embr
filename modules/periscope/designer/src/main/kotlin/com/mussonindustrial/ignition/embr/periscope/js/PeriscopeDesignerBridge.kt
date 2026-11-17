package com.mussonindustrial.ignition.embr.periscope.js

import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.mussonindustrial.embr.perspective.common.exceptions.JavaScriptException
import com.teamdev.jxbrowser.browser.Browser
import com.teamdev.jxbrowser.engine.Engine
import com.teamdev.jxbrowser.js.JsFunctionCallback
import com.teamdev.jxbrowser.js.JsObject
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

class PeriscopeDesignerBridge(engine: Engine) : DesignerWebBridge {

    private val logger = this.getLoggerEx()

    private val resourcePath = "/static/embr-periscope-designer-web.js"
    private val moduleName = "EmbrPeriscopeDesigner"

    private val thread = Executors.newSingleThreadExecutor {
        Thread(it, "embr-periscope-bridge").apply { isDaemon = true }
    }
    private val nextCallId = AtomicLong()
    private val pendingReplies = ConcurrentHashMap<String, CompletableFuture<String>>()
    private val replyCallback = JsFunctionCallback { args ->
        receiveReply(args)
        null
    }
    private val browser = CompletableFuture.supplyAsync({ engine.newBrowser() }, thread)
    private val module = browser.thenApply(::load)

    private fun load(browser: Browser): JsObject {
        val source =
            requireNotNull(javaClass.getResource(resourcePath)) {
                "Could not find $resourcePath on classpath"
            }
        browser.navigation().loadUrlAndWait("about:blank")
        val frame = browser.mainFrame().get()
        frame.executeJavaScript<Void>(source.readText())
        val module =
            frame.executeJavaScript<JsObject>(moduleName)
                ?: error("JavaScript Bridge Module not found")
        module.call<Any>("connect", replyCallback)
        return module
    }

    private fun receiveReply(args: Array<out Any?>) {
        val id = args.getOrNull(0) as? String ?: return
        val reply = pendingReplies.remove(id) ?: return
        val success = args.getOrNull(1) == true
        val value = args.getOrNull(2)?.toString().orEmpty()

        if (success) {
            reply.complete(value)
        } else {
            reply.completeExceptionally(JavaScriptException(value))
        }
    }

    override fun <T> invoke(
        service: String,
        method: String,
        payload: String,
        parse: (String) -> T,
    ): CompletableFuture<T> {
        val result = CompletableFuture<T>()
        val call = Runnable {
            if (result.isDone) return@Runnable

            val id = nextCallId.incrementAndGet().toString()
            val reply = CompletableFuture<String>()
            pendingReplies[id] = reply
            try {
                module.join().call<Any>("dispatch", id, service, method, payload)
                val json = reply.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                result.complete(parse(json))
            } catch (e: Exception) {
                logger.warn("Bridge call '$service.$method' failed.", e)
                result.completeExceptionally(e)
            } finally {
                pendingReplies.remove(id)
            }
        }
        try {
            thread.execute(call)
        } catch (e: RejectedExecutionException) {
            result.completeExceptionally(e)
        }
        return result
    }

    fun close() {
        thread.execute { browser.join().close() }
        thread.shutdown()
    }

    companion object {
        private const val TIMEOUT_SECONDS = 15L
    }
}
