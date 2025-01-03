package com.mussonindustrial.ignition.embr.periscope.component.container

import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.script.builtin.KeywordArgs
import com.inductiveautomation.ignition.common.util.LogUtil
import com.inductiveautomation.perspective.gateway.api.*
import com.inductiveautomation.perspective.gateway.messages.EventFiredMsg
import com.inductiveautomation.perspective.gateway.script.ComponentModelScriptWrapper
import com.mussonindustrial.embr.common.scripting.PyArgOverloadBuilder
import com.mussonindustrial.embr.perspective.gateway.component.ComponentDelegateJavaScriptProxy
import kotlin.reflect.typeOf
import org.python.core.PyObject

class CoordinateCanvasModelDelegate(component: Component) : ComponentModelDelegate(component) {

    private val log = LogUtil.getModuleLogger("embr-periscope", "CoordinateCanvasModelDelegate")
    private val proxies = hashMapOf<String, ComponentDelegateJavaScriptProxy>()
    private val queue = component.session.queue()
    private val methods = MethodOverloads()

    override fun onStartup() {
        component.mdc { log.debugf("Startup") }
    }

    override fun onShutdown() {
        component.mdc { log.debugf("Shutdown") }
    }

    override fun handleEvent(message: EventFiredMsg) {
        proxies.values.forEach {
            if (it.handles(message)) {
                it.handleEvent(message)
            }
        }
    }

    @ScriptCallable
    @Suppress("unused")
    fun getJavaScriptProxy(property: String): ComponentDelegateJavaScriptProxy {
        return proxies.getOrPut(property) {
            ComponentDelegateJavaScriptProxy(component, this, property)
        }
    }

    @ScriptCallable
    @KeywordArgs(
        names = ["component", "zoomToFit", "padding"],
        types = [Any::class, Boolean::class, String::class],
    )
    @Suppress("unused")
    fun fitChild(args: Array<PyObject>, keywords: Array<String>) =
        queue.submit { component.mdc { methods.fitChild.call(args, keywords) } }

    inner class MethodOverloads {
        val fitChild =
            PyArgOverloadBuilder()
                .setName("fitChild")
                .addOverload(
                    {
                        val component = it["component"]

                        var name: String? = null
                        when (component) {
                            is ComponentModelScriptWrapper.SafetyWrapper -> name = component.name
                            is String -> name = component
                        }
                        if (name == null) {
                            log.warnf("Error fitting to child. Child name cannot be null.")
                            return@addOverload null
                        }

                        val child =
                            this@CoordinateCanvasModelDelegate.component.findChildByName(
                                listOf(name).iterator()
                            )

                        if (child.isEmpty) {
                            log.warnf("Error fitting to child $name, child does not exist.")
                            return@addOverload null
                        }

                        log.info("Fitting to child $name")
                        fireEvent("fit-child", JsonObject().apply { addProperty("name", name) })
                    },
                    "component" to typeOf<Any>(),
                )
                .build()
    }
}
