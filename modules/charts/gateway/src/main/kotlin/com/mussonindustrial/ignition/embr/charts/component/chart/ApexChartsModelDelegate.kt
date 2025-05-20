package com.mussonindustrial.ignition.embr.charts.component.chart

import com.inductiveautomation.ignition.common.util.LogUtil
import com.inductiveautomation.perspective.gateway.api.Component
import com.inductiveautomation.perspective.gateway.api.ComponentModelDelegate
import com.inductiveautomation.perspective.gateway.api.ScriptCallable
import com.inductiveautomation.perspective.gateway.messages.EventFiredMsg
import com.mussonindustrial.embr.perspective.gateway.component.ComponentDelegateJavaScriptProxy
import com.mussonindustrial.embr.perspective.gateway.javascript.JavaScriptProxy
import com.mussonindustrial.embr.perspective.gateway.javascript.JavaScriptProxyable

class ApexChartsModelDelegate(component: Component) :
    ComponentModelDelegate(component), JavaScriptProxyable {

    private val logger = LogUtil.getModuleLogger("embr-charts", "ApexChartsModelDelegate")
    private val jsProxy = ComponentDelegateJavaScriptProxy(component, this)

    override fun onStartup() {
        component.mdc { logger.debugf("Startup") }
    }

    override fun onShutdown() {
        component.mdc { logger.debugf("Shutdown") }
    }

    override fun handleEvent(message: EventFiredMsg) {
        jsProxy.handleEvent(message)
    }

    @ScriptCallable
    @Suppress("unused")
    override fun getJavaScriptProxy(): JavaScriptProxy {
        return jsProxy
    }
}
