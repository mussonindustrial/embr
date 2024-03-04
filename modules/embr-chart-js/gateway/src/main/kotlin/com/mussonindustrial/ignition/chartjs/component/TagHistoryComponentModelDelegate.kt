package com.mussonindustrial.ignition.chartjs.component

import com.inductiveautomation.ignition.common.Coercable
import com.inductiveautomation.ignition.common.TypeUtilities
import com.inductiveautomation.ignition.common.gson.JsonArray
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.model.values.QualifiedValue
import com.inductiveautomation.ignition.common.tags.paths.BasicTagPath
import com.inductiveautomation.ignition.common.tags.paths.parser.TagPathParser
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

data class TagConfig(var tagPath: String)
fun asTagConfig(qv: QualifiedValue): TagConfig {
    val qvMap = TypeUtilities.coerce(qv.value, HashMap::class.java) as HashMap<String, QualifiedValue>
    return TagConfig(qvMap["tagPath"]?.value.toString())
}
fun asTagConfigList(qv: QualifiedValue): List<TagConfig> {
    val qvArray = TypeUtilities.coerce(qv.value, Array<QualifiedValue>::class.java) as Array<QualifiedValue>
    return qvArray.map {
        asTagConfig(it)
    }
}

class TagHistoryComponentModelDelegate(context: GatewayContext, component: Component?) : ComponentModelDelegate(component) {

    private lateinit var tags: PropertyReference

    companion object {
        const val MESSAGE_DATA_NEW: String = "tag-history-chart-data-new"
    }

    private val onValueChange: (changeEvent: PropertyTreeChangeEvent) -> Unit = { changeEvent ->

        val tagList = asTagConfigList(changeEvent.readValue())
        val tagPaths = tagList.map { TagPathParser.parseSafe(it.tagPath) }

        context.tagManager.readAsync(tagPaths).thenApply { results ->

            val payload = JsonObject()
            val values = JsonArray()

            results.forEach {
                values.add(TypeUtilities.coerce(it.value, Float::class.java) as Float)
            }
            payload.add("values", values)
            this.fireEvent(MESSAGE_DATA_NEW, payload)
        }
    }

    override fun onStartup() {
        log.debug("Starting up delegate for component '${component.componentAddressPath}'")
        tags = component.createPropertyReference("this.props.tags", onValueChange, Origin.ANY)
        tags.resolveReference()
        tags.startup()
    }

    override fun onShutdown() {
        log.debug("Shutting down  delegate for component '${component.componentAddressPath}'")
        tags.shutdown()
    }

}