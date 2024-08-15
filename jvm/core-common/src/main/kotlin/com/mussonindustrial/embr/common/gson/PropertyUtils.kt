package com.mussonindustrial.embr.common.gson

import com.inductiveautomation.ignition.common.config.PropertySet
import com.inductiveautomation.ignition.common.config.PropertyValue
import com.inductiveautomation.ignition.common.gson.JsonArray
import com.inductiveautomation.ignition.common.gson.JsonObject

fun JsonObject.addProperty(
    property: String,
    value: List<PropertyValue>,
) {
    val values = JsonArray()
    value.forEach {
        val json = JsonObject()
        json.addProperty(it.property.toString(), it.value.toString())
        values.add(json)
    }
    add(property, values)
}

fun JsonObject.addProperty(
    property: String,
    value: PropertySet,
) {
    val json = JsonObject()
    value.forEach {
        json.addProperty(it.property.toString(), it.value.toString())
    }
    add(property, json)
}
