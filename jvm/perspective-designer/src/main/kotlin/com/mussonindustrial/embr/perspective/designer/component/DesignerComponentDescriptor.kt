package com.mussonindustrial.embr.perspective.designer.component

import com.inductiveautomation.ignition.designer.navtree.icon.InteractiveSvgIcon
import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import java.util.Optional
import javax.swing.Icon

data class DesignerComponentDescriptor(val componentDescriptor: ComponentDescriptor) :
    ComponentDescriptor by componentDescriptor {
    private val svgIcon: InteractiveSvgIcon =
        InteractiveSvgIcon.createIcon("images/components/${componentDescriptor.id()}.icon.svg")

    override fun getIcon(): Optional<Icon> {
        return Optional.of(this.svgIcon)
    }
}

fun ComponentDescriptor.asDesignerDescriptor(): DesignerComponentDescriptor {
    return DesignerComponentDescriptor(this)
}
