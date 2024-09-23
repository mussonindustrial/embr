package com.mussonindustrial.embr.common.gson

import com.inductiveautomation.ignition.common.gson.JsonElement
import com.inductiveautomation.ignition.common.gson.JsonSerializationContext
import com.inductiveautomation.ignition.common.gson.JsonSerializer
import java.lang.reflect.Type

class SimpleGsonAdapter<T : SimpleJsonSerializable> : JsonSerializer<T> {
    override fun serialize(
        t: T,
        type: Type,
        serializationContext: JsonSerializationContext,
    ): JsonElement {
        return t.toGson()
    }
}
