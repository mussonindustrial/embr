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

object ProjectComponents {
    private const val TYPE_PREFIX = "embr.periscope.component."
    private const val PALETTE_CATEGORY = "React"
    private val COMPONENT_TYPE = Regex(""""type"\s*:\s*"${Regex.escape(TYPE_PREFIX)}([^"\\]+)"""")

    const val VIEW_FILE = "view.json"
    val VIEWS = ResourceType(PerspectiveModule.MODULE_ID, "views")
    private val LOOSE_SCHEMA = createSchema("""{"type":"object","default":{}}""")

    fun findUsedComponents(viewJson: String): Sequence<String> {
        return COMPONENT_TYPE.findAll(viewJson).map { it.groupValues[1] }
    }

    fun getType(id: String): String {
        return TYPE_PREFIX + id
    }

    fun getId(type: String): String {
        return type.removePrefix(TYPE_PREFIX)
    }

    fun createSchema(json: String): JsonSchema {
        return withContextClassLoaders(ProjectComponents::class.java.classLoader) {
            JsonSchema.parse(json.byteInputStream())
        }
    }

    fun createDescriptor(
        id: String,
        name: String = id,
        schema: JsonSchema = LOOSE_SCHEMA,
        defaults: JsonObject = JsonObject(),
        events: List<ComponentEventDescriptor> = emptyList(),
        childPositionSchema: JsonSchema? = null,
    ): ComponentDescriptor {
        val builder =
            ComponentDescriptorImpl.ComponentBuilder.newBuilder()
                .setPaletteCategory(PALETTE_CATEGORY)
                .setId(getType(id))
                .setModuleId(MODULE_ID)
                .setSchema(schema)
                .setName(name)
                .setDefaultMetaName(name)
                .setDefaultProps(defaults)
                .setEvents(events)
                .setResources(PeriscopeComponents.BROWSER_RESOURCES)

        if (childPositionSchema != null) {
            builder.setChildPositionSchema(childPositionSchema)
        }

        return builder.build()
    }
}
