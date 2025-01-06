package com.mussonindustrial.ignition.embr.charts.component.chart

import com.inductiveautomation.ignition.common.util.LogUtil
import com.inductiveautomation.perspective.common.api.PropertyType
import com.inductiveautomation.perspective.common.config.EventConfig
import com.inductiveautomation.perspective.common.property.Origin
import com.inductiveautomation.perspective.gateway.api.Component
import com.inductiveautomation.perspective.gateway.api.ComponentModelDelegate
import com.inductiveautomation.perspective.gateway.api.ScriptCallable
import com.inductiveautomation.perspective.gateway.binding.BindingUtils.toJsonDeep
import com.inductiveautomation.perspective.gateway.messages.EventFiredMsg
import com.inductiveautomation.perspective.gateway.property.PropertyTree
import com.inductiveautomation.perspective.gateway.property.PropertyTree.Subscription
import com.mussonindustrial.embr.perspective.gateway.component.ComponentDelegateJavaScriptProxy
import com.mussonindustrial.ignition.embr.charts.ChartsGatewayContext
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class SmoothieChartModelDelegate(component: Component) : ComponentModelDelegate(component) {

    private val log = LogUtil.getModuleLogger("embr-charts", "SmoothieChartModelDelegate")
    private val context = ChartsGatewayContext.instance.perspectiveContext
    private val props = PropsHandler(component.getPropertyTreeOf(PropertyType.props)!!)
    private val proxies = hashMapOf<String, ComponentDelegateJavaScriptProxy>()

    private var scheduledUpdate: ScheduledFuture<*>? = null
    private val intervalListener: Subscription = createIntervalListener()

    override fun onStartup() {
        component.mdc { log.debugf("Startup") }
        scheduleUpdate()
    }

    override fun onShutdown() {
        component.mdc { log.debugf("Shutdown") }
        scheduledUpdate?.cancel(false)
        intervalListener.unsubscribe()
    }

    override fun handleEvent(message: EventFiredMsg) {
        proxies.values.forEach {
            if (it.handles(message)) {
                it.handleEvent(message)
            }
        }
    }

    private fun createIntervalListener(): Subscription {
        return props.tree.subscribe("options.update.interval", Origin.allBut(Origin.Delegate)) {
            scheduledUpdate?.cancel(false)

            if (props.updateInterval <= 0) {
                return@subscribe
            }

            scheduleUpdate()
        }
    }

    private fun scheduleUpdate() {
        scheduledUpdate =
            context.scheduler.scheduleWithFixedDelay(
                { onChartUpdate() },
                0,
                props.updateInterval,
                TimeUnit.MILLISECONDS
            )
    }

    inner class ChartDataEvent {
        @Suppress("unused") val chart = getJavaScriptProxy("chart")
    }

    private fun onChartUpdate() {
        component.fireEvent(EventConfig.COMPONENT_EVENTS, "onChartUpdate", ChartDataEvent())
    }

    @ScriptCallable
    @Suppress("unused")
    fun getJavaScriptProxy(property: String): ComponentDelegateJavaScriptProxy {
        return proxies.getOrPut(property) {
            ComponentDelegateJavaScriptProxy(component, this, property)
        }
    }

    inner class PropsHandler(val tree: PropertyTree) {
        val updateInterval: Long
            get() {
                val viewPath = tree.read("options.update.interval")
                if (viewPath.isEmpty) {
                    return 1000
                }

                return toJsonDeep(viewPath.get()).asLong
            }
    }
}
