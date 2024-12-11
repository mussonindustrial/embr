package com.mussonindustrial.ignition.embr.charts.component.chart

import com.inductiveautomation.ignition.common.util.LogUtil
import com.inductiveautomation.perspective.gateway.api.Component
import com.inductiveautomation.perspective.gateway.api.ComponentModelDelegate
import com.inductiveautomation.perspective.gateway.api.ScriptCallable
import com.inductiveautomation.perspective.gateway.messages.EventFiredMsg
import org.python.core.PyObject

class ChartJsModelDelegate(component: Component) :
    ComponentModelDelegate(component), JavaScriptProxy {

    private val log = LogUtil.getModuleLogger("embr-charts", "ChartJsModelDelegate")
    private val jsProxy = JavaScriptDelegate(component, this)

    override fun onStartup() {
        component.mdc { log.debugf("Startup") }
    }

    override fun onShutdown() {
        component.mdc { log.debugf("Shutdown") }
    }

    override fun handleEvent(message: EventFiredMsg) {
        if (jsProxy.handles(message)) {
            jsProxy.handleEvent(message)
        }
    }

    @ScriptCallable
    @Suppress("unused")
    override fun runJavaScriptAsync(args: Array<PyObject>, keywords: Array<String>) =
        jsProxy.runJavaScriptAsync(args, keywords)

    @ScriptCallable
    @Suppress("unused")
    override fun runJavaScriptBlocking(args: Array<PyObject>, keywords: Array<String>) =
        jsProxy.runJavaScriptBlocking(args, keywords)
}
