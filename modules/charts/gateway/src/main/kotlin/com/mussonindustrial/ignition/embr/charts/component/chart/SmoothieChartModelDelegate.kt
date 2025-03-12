package com.mussonindustrial.ignition.embr.charts.component.chart

import com.inductiveautomation.ignition.common.TypeUtilities
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.util.LogUtil
import com.inductiveautomation.perspective.gateway.api.Component
import com.inductiveautomation.perspective.gateway.api.ComponentModelDelegate
import com.inductiveautomation.perspective.gateway.api.ScriptCallable
import com.inductiveautomation.perspective.gateway.binding.BindingUtils
import com.inductiveautomation.perspective.gateway.binding.BindingUtils.toJsonDeep
import com.inductiveautomation.perspective.gateway.messages.EventFiredMsg
import com.mussonindustrial.embr.perspective.gateway.component.ComponentDelegateJavaScriptProxy
import org.python.core.PySequence

class SmoothieChartModelDelegate(component: Component) : ComponentModelDelegate(component) {

    private val log = LogUtil.getModuleLogger("embr-charts", "SmoothieChartModelDelegate")
    private val proxies = hashMapOf<String, ComponentDelegateJavaScriptProxy>()

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

    inner class ChartJavaScriptProxy :
        ComponentDelegateJavaScriptProxy(component, this@SmoothieChartModelDelegate, "chart") {

        @Suppress("unused")
        fun appendData(values: PySequence) {
            val json =
                toJsonDeep(
                    TypeUtilities.pyToJava(values),
                    BindingUtils.JsonEncoding.DollarQualified
                )
            this@SmoothieChartModelDelegate.fireEvent(
                "data-append",
                JsonObject().apply { add("values", json) }
            )
        }
    }

    @ScriptCallable
    @Suppress("unused")
    fun getJavaScriptProxy(property: String): ComponentDelegateJavaScriptProxy {
        return proxies.getOrPut(property) {
            when (property) {
                "chart" -> ChartJavaScriptProxy()
                else -> ComponentDelegateJavaScriptProxy(component, this, property)
            }
        }
    }
}
