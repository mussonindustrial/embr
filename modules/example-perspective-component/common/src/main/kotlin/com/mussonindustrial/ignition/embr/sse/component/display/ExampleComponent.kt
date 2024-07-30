package com.mussonindustrial.ignition.embr.sse.component.display

import com.inductiveautomation.ignition.common.jsonschema.JsonSchema
import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl
import com.mussonindustrial.ignition.embr.sse.Components
import com.mussonindustrial.ignition.embr.sse.Meta.MODULE_ID

class ExampleComponent {
    companion object {
        val id: String = "mussonindustrial.display.example"
        var schema: JsonSchema = JsonSchema.parse(Components::class.java.getResourceAsStream("/example-component.props.json"))
        var descriptor: ComponentDescriptor =
            ComponentDescriptorImpl.ComponentBuilder.newBuilder()
                .setPaletteCategory(Components.componentCategory)
                .setId(id)
                .setModuleId(MODULE_ID)
                .setSchema(schema)
                .setName("Example")
//            .setIcon(ImageIcon(Components::class.java.getResource("/icons/simplecomponent.png")))
                .addPaletteEntry("", "Example", "An example Perspective component.", null, null)
                .setDefaultMetaName("Example")
                .setResources(Components.browserResources)
                .build()
    }
}
