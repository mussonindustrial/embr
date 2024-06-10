package com.mussonindustrial.ignition.embr.sse.component.display

import com.inductiveautomation.ignition.common.jsonschema.JsonSchema
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl
import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.mussonindustrial.ignition.embr.sse.Components
import com.mussonindustrial.ignition.embr.sse.Components.COMPONENT_CATEGORY
import com.mussonindustrial.ignition.embr.sse.Meta.MODULE_ID

class ExampleComponent {
    companion object {
        var COMPONENT_ID: String = "mussonindustrial.display.example"
        var SCHEMA: JsonSchema = JsonSchema.parse(Components::class.java.getResourceAsStream("/example-component.props.json"))
        var DESCRIPTOR: ComponentDescriptor = ComponentDescriptorImpl.ComponentBuilder.newBuilder()
            .setPaletteCategory(COMPONENT_CATEGORY)
            .setId(COMPONENT_ID)
            .setModuleId(MODULE_ID)
            .setSchema(SCHEMA)
            .setName("Example")
//            .setIcon(ImageIcon(Components::class.java.getResource("/icons/simplecomponent.png")))
            .addPaletteEntry("", "Example", "An example Perspective component.", null, null)
            .setDefaultMetaName("Example")
            .setResources(Components.BROWSER_RESOURCES)
            .build()
    }
}