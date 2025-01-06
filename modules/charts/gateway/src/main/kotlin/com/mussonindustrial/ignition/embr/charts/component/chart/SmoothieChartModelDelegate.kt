package com.mussonindustrial.ignition.embr.charts.component.chart

import com.inductiveautomation.ignition.common.TypeUtilities
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.util.LogUtil
import com.inductiveautomation.perspective.common.api.PropertyType
import com.inductiveautomation.perspective.common.config.EventConfig
import com.inductiveautomation.perspective.gateway.api.Component
import com.inductiveautomation.perspective.gateway.api.ComponentModelDelegate
import com.inductiveautomation.perspective.gateway.api.ScriptCallable
import com.inductiveautomation.perspective.gateway.binding.BindingUtils.toJsonDeep
import com.inductiveautomation.perspective.gateway.messages.EventFiredMsg
import com.inductiveautomation.perspective.gateway.property.PropertyTree
import com.mussonindustrial.embr.perspective.gateway.component.ComponentDelegateJavaScriptProxy
import com.mussonindustrial.ignition.embr.charts.ChartsGatewayContext
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import org.python.core.PySequence

class SmoothieChartModelDelegate(component: Component) : ComponentModelDelegate(component) {

    private val log = LogUtil.getModuleLogger("embr-charts", "SmoothieChartModelDelegate")
    private val context = ChartsGatewayContext.instance.perspectiveContext
    private val queue = component.session.queue()
    private val props = PropsHandler(component.getPropertyTreeOf(PropertyType.props)!!)
    private val proxies = hashMapOf<String, ComponentDelegateJavaScriptProxy>()
    private var updater: ScheduledFuture<*>? = null

    private val UPDATE_DATA_MESSAGE = "update-data"

    override fun onStartup() {
        component.mdc { log.debugf("Startup") }
        updater =
            context.scheduler.scheduleWithFixedDelay(
                { getChartData() },
                0,
                props.delayMillis,
                TimeUnit.MILLISECONDS
            )
    }

    override fun onShutdown() {
        component.mdc { log.debugf("Shutdown") }
        updater?.cancel(false)
    }

    fun sendData(values: PySequence) {
        queue.submit {
            val json = JsonObject().apply { add("values", TypeUtilities.pyToGson(values)) }
            this.fireEvent(UPDATE_DATA_MESSAGE, json)
        }
    }

    override fun handleEvent(message: EventFiredMsg) {
        proxies.values.forEach {
            if (it.handles(message)) {
                it.handleEvent(message)
            }
        }
    }

    inner class ChartDataEvent {
        @Suppress("unused")
        fun sendData(values: PySequence) {
            this@SmoothieChartModelDelegate.sendData(values)
        }
    }

    private fun getChartData() {
        component.fireEvent(EventConfig.COMPONENT_EVENTS, "getChartData", ChartDataEvent())
    }

    @ScriptCallable
    @Suppress("unused")
    fun getJavaScriptProxy(property: String): ComponentDelegateJavaScriptProxy {
        return proxies.getOrPut(property) {
            ComponentDelegateJavaScriptProxy(component, this, property)
        }
    }

    inner class PropsHandler(private val tree: PropertyTree) {
        val delayMillis: Long
            get() {
                val viewPath = tree.read("options.delayMillis")
                if (viewPath.isEmpty) {
                    return 0
                }

                return toJsonDeep(viewPath.get()).asLong
            }
    }
}
