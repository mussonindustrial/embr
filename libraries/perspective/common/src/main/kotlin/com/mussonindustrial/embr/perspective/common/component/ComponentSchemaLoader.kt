package com.mussonindustrial.embr.perspective.common.component

import com.inductiveautomation.ignition.common.jsonschema.JsonSchema
import com.inductiveautomation.perspective.common.api.ComponentEventDescriptor

class ComponentSchemaLoader(
    private val clazz: Class<*>,
    private val componentId: String,
) {
    fun getSchema(): JsonSchema {
        return getJsonSchema("/schemas/components/${componentId}/props.json")
    }

    fun getEventDescriptor(name: String, description: String): ComponentEventDescriptor {
        return ComponentEventDescriptor(
            name,
            description,
            getJsonSchema("/schemas/components/${componentId}/events/$name.props.json")
        )
    }

    fun getPaletteEntry(variantId: String, label: String, tooltip: String): PaletteEntry {
        return PaletteEntry(
            ComponentSchemaLoader::class.java,
            componentId,
            variantId,
            label,
            tooltip
        )
    }

    private fun getJsonSchema(path: String): JsonSchema {
        return JsonSchema.parse(ComponentSchemaLoader::class.java.getResourceAsStream(path))
    }
}
