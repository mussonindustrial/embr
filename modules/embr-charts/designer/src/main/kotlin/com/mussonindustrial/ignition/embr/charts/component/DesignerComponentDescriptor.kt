package com.mussonindustrial.ignition.embr.charts.component

import com.inductiveautomation.ignition.designer.navtree.icon.InteractiveSvgIcon
import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.mussonindustrial.ignition.embr.charts.Meta
import java.util.*
import javax.swing.Icon

data class DesignerComponentDescriptor(val componentDescriptor: ComponentDescriptor):
    ComponentDescriptor by componentDescriptor {
    override fun getIcon(): Optional<Icon> {
        val icon = InteractiveSvgIcon(Meta::class.java, "images/svgicons/${componentDescriptor.id()}.icon.svg")
        return Optional.of(icon)
    }
}