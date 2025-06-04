package com.mussonindustrial.ignition.embr.periscope.component.embedding

import com.inductiveautomation.ignition.common.util.LogUtil
import com.inductiveautomation.perspective.gateway.api.Component
import com.inductiveautomation.perspective.gateway.api.ComponentModelDelegate
import com.inductiveautomation.perspective.gateway.api.ScriptCallable
import com.inductiveautomation.perspective.gateway.messages.EventFiredMsg
import com.mussonindustrial.embr.perspective.gateway.component.ComponentDelegateJavaScriptProxy

class SwiperModelDelegate(component: Component) : ComponentModelDelegate(component) {

    private val log = LogUtil.getModuleLogger("embr-periscope", "SwiperModelDelegate")
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

    @ScriptCallable
    @Suppress("unused")
    fun getJavaScriptProxy(property: String): ComponentDelegateJavaScriptProxy {
        return proxies.getOrPut(property) {
            ComponentDelegateJavaScriptProxy(component, this, property)
        }
    }
}
