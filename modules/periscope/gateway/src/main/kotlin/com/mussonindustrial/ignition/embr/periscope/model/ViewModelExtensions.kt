package com.mussonindustrial.ignition.embr.periscope.model

import com.inductiveautomation.ignition.common.JsonPath
import com.inductiveautomation.ignition.common.gson.JsonElement
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.util.ExecutionQueue
import com.inductiveautomation.perspective.common.api.PropertyType
import com.inductiveautomation.perspective.common.config.ParamType
import com.inductiveautomation.perspective.common.property.Origin
import com.inductiveautomation.perspective.gateway.binding.BindingUtils.toJsonDeep
import com.inductiveautomation.perspective.gateway.model.ViewModel
import com.inductiveautomation.perspective.gateway.property.PropertyTree
import com.inductiveautomation.perspective.gateway.property.PropertyTreeChangeEvent

fun ViewModel.writeToParams(
    params: JsonObject,
    origin: Origin,
    source: Any,
    queue: ExecutionQueue
) {
    val tree = this.getPropertyTreeOf(PropertyType.params) ?: return
    val currentParams = toJsonDeep(tree.read(JsonPath.ROOT).get().value) as JsonObject
    val inputKeys =
        this.config.paramDefinitions
            .filter { (_, type) -> type == ParamType.input || type == ParamType.inout }
            .map { (key, _) -> key }
            .toList()

    val writes = JsonObject()
    inputKeys.forEach {
        val currentValue = currentParams.get(it) ?: null
        val newValue = params.get(it) ?: null
        if (currentValue != newValue) {
            writes.add(it, newValue)
        }
        writes.add(it, newValue)
    }

    if (!writes.isEmpty) {
        tree.writeAll(writes, origin, source)
    }
}

fun ViewModel.writeToParams(
    param: String,
    value: JsonElement,
    origin: Origin,
    source: Any,
    queue: ExecutionQueue
) {

    val paramType = this.config.paramDefinitions.toList().find { (key, _) -> key == param }?.value
    if (paramType != ParamType.input && paramType != ParamType.inout) {
        return
    }

    val tree = this.getPropertyTreeOf(PropertyType.params) ?: return
    tree.write(param, value, origin, source)
}

fun ViewModel.subscribeToParams(
    acceptableOrigins: Set<Origin>,
    block: (PropertyTreeChangeEvent) -> Unit
): Map<String, PropertyTree.Subscription>? {
    val tree = this.getPropertyTreeOf(PropertyType.params) ?: return null

    val outputKeys =
        this.config.paramDefinitions
            .filter { (_, type) -> type.isOut }
            .map { (key, _) -> key }
            .toList()

    if (this.logger.isTraceEnabled) {
        mdc { this.logger.trace("Creating subscriptions for ${tree.rootKeys}") }
    }

    val listeners =
        outputKeys.associateWith { rootKey -> tree.subscribe(rootKey, acceptableOrigins, block) }

    return listeners
}

fun ViewModel.readParams(): JsonObject? {
    val tree = this.getPropertyTreeOf(PropertyType.params) ?: return null

    val maybeRoot = tree.read(JsonPath.ROOT)
    if (maybeRoot.isEmpty) {
        return null
    }

    return toJsonDeep(maybeRoot.get()).asJsonObject
}
