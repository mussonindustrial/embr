import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl
import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.mussonindustrial.ignition.perspective.example.Components

class SimpleComponent {
    companion object {
        // unique ID of the component which perfectly matches that provided in the javascript's ComponentMeta implementation
        var COMPONENT_ID: String = "mussonindustrial.display.simplecomponent"
//        var SCHEMA: JsonSchema = JsonSchema.parse(Components::class.javaClass.getResourceAsStream("/simplecomponent.props.json"))
        var DESCRIPTOR: ComponentDescriptor = ComponentDescriptorImpl.ComponentBuilder.newBuilder()
            .setPaletteCategory(Components.COMPONENT_CATEGORY)
            .setId(COMPONENT_ID)
            .setModuleId(Components.MODULE_ID)
//            .setSchema(SCHEMA) //  this could alternatively be created purely in Java if desired
            .setName("SimpleComponent")
//            .setIcon(ImageIcon(Components::class.java.getResource("/icons/simplecomponent.png")))
            .addPaletteEntry("", "SimpleComponent", "An testing component.", null, null)
            .setDefaultMetaName("simpleComponent")
            .setResources(Components.BROWSER_RESOURCES)
            .build()
    }
}