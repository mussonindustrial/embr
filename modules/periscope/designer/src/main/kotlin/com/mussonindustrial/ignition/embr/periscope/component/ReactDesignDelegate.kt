package com.mussonindustrial.ignition.embr.periscope.component

import com.inductiveautomation.ignition.client.jsonedit.DocumentModel
import com.inductiveautomation.ignition.common.gson.Gson
import com.inductiveautomation.ignition.common.util.get
import com.inductiveautomation.perspective.common.api.PropertyType
import com.inductiveautomation.perspective.common.util.PropertyJsonUtil
import com.inductiveautomation.perspective.designer.api.ComponentDesignDelegate
import com.inductiveautomation.perspective.designer.workspace.ComponentSelection
import com.mussonindustrial.embr.common.logging.getLogger
import com.mussonindustrial.ignition.embr.periscope.PeriscopeDesignerContext
import com.teamdev.jxbrowser.browser.Browser
import com.teamdev.jxbrowser.engine.Engine
import com.teamdev.jxbrowser.js.JsFunctionCallback
import com.teamdev.jxbrowser.js.JsObject
import com.teamdev.jxbrowser.view.swing.BrowserView
import java.io.File
import javax.swing.JComponent

class ReactDesignDelegate(val context: PeriscopeDesignerContext) : ComponentDesignDelegate {

    val logger = this.getLogger()
    val engine: Engine = context.perspectiveDesignerHook.workspace.engine
    val browser: Browser = engine.newBrowser()
    val fileUrl: String
    val view: BrowserView

    init {
        logger.info("Creating ReactDesignDelegate")
        val resourceStream = javaClass.getResourceAsStream("/static/editor.html")
        val tempHtmlFile =
            File.createTempFile("editor", ".html").apply {
                if (resourceStream != null) {
                    outputStream().use { output -> resourceStream.copyTo(output) }
                }
                deleteOnExit()
            }

        fileUrl = tempHtmlFile.toURI().toString()
        browser.navigation().loadUrl(fileUrl)
        browser.resize(100, 100)
        view = BrowserView.newInstance(browser)
    }

    override fun installDocumentListener(scope: PropertyType, documentModel: DocumentModel) {
        if (scope == PropertyType.props) {
            logger.info("Registering node change listener on $documentModel")
            documentModel.addNodeChangeListener { node, propertyName, previousValue, newValue ->
                logger.info("Nodes Changed: $node, $propertyName, $previousValue, $newValue")

                val decoded = PropertyJsonUtil.decodeQualifiedValueJson(documentModel.asJsonTree)
                val component = decoded?.get("component")
                val componentString = component?.asString ?: "FAILED TO PARSE COMPONENT"

                logger.info("Setting into browser: $componentString")
                setModel(componentString)
            }
        }
    }

    fun setModel(value: String) {
        view.browser.mainFrame().ifPresent { frame ->
            val model: JsObject? = frame.executeJavaScript("window.editor.ref.getModel()")
            if (model == null) {
                logger.info("Unable to load model")
                return@ifPresent
            }
            model.call("setValue", value)
        }
    }

    fun tryCallback(block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            logger.error("Callback error", e)
        }
    }

    override fun createSelectionEditor(selection: ComponentSelection): JComponent {
        logger.info("Creating selection editor")

        view.browser.mainFrame().ifPresent { frame ->
            val editor: JsObject? = frame.executeJavaScript("window.editor")

            val props = selection.componentDetails.first().props
            val decoded = PropertyJsonUtil.decodeQualifiedValueJson(props)
            val component = decoded?.get("component")
            val componentString = component?.asString ?: "FAILED TO PARSE COMPONENT"

            editor?.let {
                it.putProperty(
                    "onMount",
                    object : JsFunctionCallback {
                        override fun invoke(vararg args: Any?) {
                            tryCallback { logger.info("onMount") }
                        }
                    },
                )
                it.putProperty("defaultValue", componentString)
            }

            editor?.putProperty(
                "onChange",
                object : JsFunctionCallback {
                    override fun invoke(vararg args: Any?) {
                        tryCallback {
                            val value = args[0].toString()
                            val safeValue = Gson().toJson(value)
                            val json = """{ "$": ["qv"], "${'$'}v": $safeValue }"""

                            selection.write(PropertyType.props, "component", json)
                        }
                    }
                },
            )

            logger.info("Setting model: $componentString")
            setModel(componentString)
        }

        return view
    }
}
