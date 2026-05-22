package com.mussonindustrial.ignition.embr.periscope.js

import com.mussonindustrial.embr.common.crypto.sha256
import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.mussonindustrial.ignition.embr.periscope.resources.compiler.CompilerBridge
import com.teamdev.jxbrowser.browser.Browser
import com.teamdev.jxbrowser.engine.Engine
import com.teamdev.jxbrowser.js.JsObject
import com.teamdev.jxbrowser.js.JsPromise
import java.net.URL

class PeriscopeDesignerBridge(engine: Engine) : CompilerBridge {

    val logger = this.getLoggerEx()

    private val resourcePath = "/static/embr-periscope-designer-web.js"
    private val editorPath = "/static/index"
    private val moduleName = "EmbrPeriscopeDesigner"

    val resource: URL =
        requireNotNull(object {}.javaClass.getResource(resourcePath)) {
            "Could not find $resourcePath on classpath"
        }
    val versionHash = resource.readText().sha256()

    val browser: Browser = createBrowser(engine)
    val frame = browser.mainFrame().get()

    val module: JsObject by lazy {
        frame.executeJavaScript<JsObject>(moduleName) ?: error("JavaScript Bridge Module not found")
    }

    fun createBrowser(engine: Engine): Browser {
        val browser = engine.newBrowser()
        browser.navigation().loadUrl("about:blank")

        val source = resource.readText()
        browser.mainFrame().get().executeJavaScript<Void>(source)

        return browser
    }

    override fun invoke(compiler: String, method: String, payload: String): String {
        val promise = module.call<JsPromise>("invoke", compiler, method, payload)
        val resultJson = promise.await { it[0] as String }
        return resultJson
    }
}
