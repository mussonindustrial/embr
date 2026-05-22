package com.mussonindustrial.ignition.embr.periscope.icons

import com.inductiveautomation.ignition.designer.navtree.icon.InteractiveSvgIcon
import com.mussonindustrial.ignition.embr.periscope.Meta

object PeriscopeIcons {
    private fun icon(name: String) = InteractiveSvgIcon(Meta::class.java, "images/svgicons/$name")

    val tsx = icon("tsx.svg")
    val css = icon("css.svg")
    val clientResourceFolderClosed = icon("client-resource-folder-closed.svg")
    val clientResourceFolderOpened = icon("client-resource-folder-opened.svg")
}
