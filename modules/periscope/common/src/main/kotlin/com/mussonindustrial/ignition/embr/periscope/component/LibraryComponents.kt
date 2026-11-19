package com.mussonindustrial.ignition.embr.periscope.component

import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.jsonschema.JsonSchema
import com.inductiveautomation.ignition.common.resourcecollection.ResourceType
import com.inductiveautomation.perspective.common.PerspectiveModule
import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl
import com.inductiveautomation.perspective.common.api.ComponentEventDescriptor
import com.mussonindustrial.embr.common.reflect.withContextClassLoaders
import com.mussonindustrial.ignition.embr.periscope.Meta.MODULE_ID
import com.mussonindustrial.ignition.embr.periscope.PeriscopeComponents

object LibraryComponents {
    private const val TYPE_PREFIX = "embr.periscope.component-library."
    private const val PALETTE_CATEGORY = "React"
    private val componentTypeRegex =
        Regex(""""type"\s*:\s*"${Regex.escape(TYPE_PREFIX)}([^"\\]+)"""")

    const val VIEW_FILE = "view.json"
    val VIEWS = ResourceType(PerspectiveModule.MODULE_ID, "views")
    val LOOSE_SCHEMA = schema("""{"type":"object","default":{}}""")

    fun usedIn(viewJson: String): Sequence<String> {
        return componentTypeRegex.findAll(viewJson).map { it.groupValues[1] }
    }

    fun typeOf(id: String): String {
        return TYPE_PREFIX + id
    }

    fun idOf(type: String): String {
        return type.removePrefix(TYPE_PREFIX)
    }

    fun schema(json: String): JsonSchema {
        return withContextClassLoaders(LibraryComponents::class.java.classLoader) {
            JsonSchema.parse(json.byteInputStream())
        }
    }

    fun descriptor(
        id: String,
        name: String,
        schema: JsonSchema,
        defaults: JsonObject,
        events: List<ComponentEventDescriptor>,
        children: JsonSchema?,
    ): ComponentDescriptor {
        val builder =
            ComponentDescriptorImpl.ComponentBuilder.newBuilder()
                .setPaletteCategory(PALETTE_CATEGORY)
                .setId(typeOf(id))
                .setModuleId(MODULE_ID)
                .setSchema(schema)
                .setName(name)
                .setDefaultMetaName(name)
                .setDefaultProps(defaults)
                .setEvents(events)
                .setResources(PeriscopeComponents.BROWSER_RESOURCES)

        if (children != null) {
            builder.setChildPositionSchema(children)
        }

        return builder.build()
    }
}
