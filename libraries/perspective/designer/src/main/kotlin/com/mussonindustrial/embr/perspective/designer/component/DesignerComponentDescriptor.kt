package com.mussonindustrial.embr.perspective.designer.component

import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.jsonschema.JsonSchema
import com.inductiveautomation.ignition.designer.navtree.icon.InteractiveSvgIcon
import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.inductiveautomation.perspective.common.api.ComponentEventDescriptor
import com.inductiveautomation.perspective.common.api.ExtensionFunctionDescriptor
import java.util.*
import javax.swing.Icon

data class DesignerComponentDescriptor(val componentDescriptor: ComponentDescriptor) :
    ComponentDescriptor by componentDescriptor {

    private val svgIcon: InteractiveSvgIcon =
        InteractiveSvgIcon.createIcon(
            "images/components/${componentDescriptor.id()}/component.icon.svg"
        )

    override fun getIcon(): Optional<Icon> {
        return Optional.of(this.svgIcon)
    }

    /* Need manual overrides for methods with default implementations
    to resolve https://youtrack.jetbrains.com/issue/KT-18324 */
    override fun childPositionSchema(): JsonSchema? {
        return componentDescriptor.childPositionSchema()
    }

    override fun events(): MutableCollection<ComponentEventDescriptor> {
        return componentDescriptor.events()
    }

    override fun extensionFunctions(): MutableCollection<ExtensionFunctionDescriptor> {
        return componentDescriptor.extensionFunctions()
    }

    override fun getInitialProps(variantId: String): JsonObject? {
        return componentDescriptor.getInitialProps(variantId)
    }
}

fun ComponentDescriptor.asDesignerDescriptor(): DesignerComponentDescriptor {
    return DesignerComponentDescriptor(this)
}
