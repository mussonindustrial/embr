package com.mussonindustrial.ignition.chartjs

import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.inductiveautomation.perspective.common.property.Origin
import com.inductiveautomation.perspective.gateway.api.Component
import com.inductiveautomation.perspective.gateway.api.ComponentModelDelegate
import com.inductiveautomation.perspective.gateway.api.ComponentModelDelegateFactory
import com.inductiveautomation.perspective.gateway.property.PropertyTreeChangeEvent

class TagHistoryComponentModelDelegateFactory(private val context: GatewayContext): ComponentModelDelegateFactory {
    override fun create(component: Component): ComponentModelDelegate {
        return TagHistoryComponentModelDelegate(context, component)
    }
}

class TagHistoryComponentModelDelegate(context: GatewayContext, component: Component?) : ComponentModelDelegate(component) {

    companion object {
        const val MESSAGE_DATA_NEW: String = "tag-history-chart-data-new"
    }

    private val onValueChange: (changeEvent: PropertyTreeChangeEvent) -> Unit = {
//        context.tagHistoryManager.queryHistory()
    }

    override fun onStartup() {
        // Called when the Gateway's ComponentModel starts.  The start itself happens when the client project is
        // loading and includes an instance of the component type in the page/view being started.
        log.debugf("Starting up delegate for '%s'!", component.componentAddressPath)
        component.createPropertyReference("this.props.tags", onValueChange, Origin.ANY)
    }

    override fun onShutdown() {
        // Called when the component is removed from the page/view and the model is shutting down.
        log.debugf("Shutting down delegate for '%s'!", component.componentAddressPath)
    }

}