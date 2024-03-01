package com.mussonindustrial.ignition.chartjs

import com.inductiveautomation.ignition.common.gson.JsonArray
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.model.values.QualifiedValue
import com.inductiveautomation.ignition.common.tags.paths.BasicTagPath
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.inductiveautomation.perspective.common.property.Origin
import com.inductiveautomation.perspective.gateway.api.Component
import com.inductiveautomation.perspective.gateway.api.ComponentModelDelegate
import com.inductiveautomation.perspective.gateway.api.ComponentModelDelegateFactory
import com.inductiveautomation.perspective.gateway.property.PropertyReference
import com.inductiveautomation.perspective.gateway.property.PropertyTreeChangeEvent

class TagHistoryComponentModelDelegateFactory(private val context: GatewayContext): ComponentModelDelegateFactory {
    override fun create(component: Component): ComponentModelDelegate {
        return TagHistoryComponentModelDelegate(context, component)
    }
}

class TagHistoryComponentModelDelegate(context: GatewayContext, component: Component?) : ComponentModelDelegate(component) {

    private lateinit var tags: PropertyReference

    companion object {
        const val MESSAGE_DATA_NEW: String = "tag-history-chart-data-new"
    }

    private val onValueChange: (changeEvent: PropertyTreeChangeEvent) -> Unit = { changeEvent ->

        changeEvent.json
        changeEvent.readValue()
        changeEvent.readCausalValue()

//        val value = changeEvent.readValue().value as QualifiedValue

        log.infof("onValueChanged fired, ${changeEvent.path}: ${changeEvent.json.asJsonObject} -> ${changeEvent.readValue()} || ${changeEvent.readCausalValue()}")

        val test = (tags.read().get().value as Array<QualifiedValue>).map { it.value }
        log.info("property read: ${test}")

//        val tagPaths = value.map {
//            log.infof("map, it: $value")
//            BasicTagPath((it.value as JsonObject).get("tagPath").toString())
//        }
//
//        context.tagManager.readAsync(tagPaths).thenApply { results ->
//
//            log.infof("Tags read: $results")
//
//            val props = JsonObject()
//            val values = JsonArray()
//            results.forEach {
//                values.add(it.value as Float)
//            }
//            props.add("values", values)
//
//            this.fireEvent(MESSAGE_DATA_NEW, props)
//        }
    }

    override fun onStartup() {
        log.infof("Starting up delegate for '%s'!", component.componentAddressPath)
        tags = component.createPropertyReference("this.props.tags", onValueChange, Origin.ANY)
        tags.resolveReference()
        tags.startup()
    }

    override fun onShutdown() {
        log.infof("Shutting down delegate for '%s'!", component.componentAddressPath)
        tags.shutdown()
    }

}