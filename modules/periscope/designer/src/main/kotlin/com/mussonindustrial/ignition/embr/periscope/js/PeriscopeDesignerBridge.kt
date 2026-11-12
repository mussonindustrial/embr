package com.mussonindustrial.ignition.embr.periscope.js

import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.teamdev.jxbrowser.browser.Browser
import com.teamdev.jxbrowser.engine.Engine
import com.teamdev.jxbrowser.js.JsObject
import com.teamdev.jxbrowser.js.JsPromise
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException

class PeriscopeDesignerBridge(engine: Engine) : DesignerWebBridge {

    private val logger = this.getLoggerEx()

    private val resourcePath = "/static/embr-periscope-designer-web.js"
    private val moduleName = "EmbrPeriscopeDesigner"

    private val thread = Executors.newSingleThreadExecutor {
        Thread(it, "embr-periscope-bridge").apply { isDaemon = true }
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
        return frame.executeJavaScript<JsObject>(moduleName)
            ?: error("JavaScript Bridge Module not found")
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
            try {
                val promise = module.join().call<JsPromise>("invoke", service, method, payload)
                result.complete(parse(promise.await { it[0] as String }))
            } catch (e: Exception) {
                logger.warn("Bridge call '$service.$method' failed.", e)
                result.completeExceptionally(e)
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
}
