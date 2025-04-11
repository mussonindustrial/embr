package com.mussonindustrial.embr.perspective.common.component

import com.inductiveautomation.ignition.common.jsonschema.JsonSchema
import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.mussonindustrial.embr.common.reflect.withContextClassLoaders

interface PerspectiveComponent {
    val id: String
    val schema: JsonSchema
        get() {
            return withContextClassLoaders(this::class.java.classLoader) {
                JsonSchema.parse(
                    this::class.java.getResourceAsStream("/schemas/components/${id}/props.json")
                )
            }
        }

    val descriptor: ComponentDescriptor
}
