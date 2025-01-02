package com.mussonindustrial.ignition.embr.periscope.component.container

import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.script.builtin.KeywordArgs
import com.inductiveautomation.ignition.common.util.LogUtil
import com.inductiveautomation.perspective.gateway.api.*
import com.mussonindustrial.embr.common.scripting.PyArgOverloadBuilder
import com.mussonindustrial.ignition.embr.periscope.PeriscopeGatewayContext
import kotlin.reflect.typeOf
import org.python.core.PyObject

class CoordinateCanvasModelDelegate(component: Component) : ComponentModelDelegate(component) {

    private val log = LogUtil.getModuleLogger("embr-periscope", "CoordinateCanvasModelDelegate")
    private val context = PeriscopeGatewayContext.instance
    private val queue = component.session.queue()
    private val methods = MethodOverloads()

    override fun onStartup() {
        component.mdc { log.debugf("Startup") }
    }

    override fun onShutdown() {
        component.mdc { log.debugf("Shutdown") }
    }

    @ScriptCallable
    @KeywordArgs(
        names = ["name"],
        types = [String::class],
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
                        val name = it["name"] as String
                        log.info("Fitting to child $name")
                        fireEvent(
                            "fit-child",
                            JsonObject().apply { addProperty("addressPath", name) }
                        )
                    },
                    "name" to typeOf<String>(),
                )
                .build()
    }
}
