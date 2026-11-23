package com.mussonindustrial.ignition.embr.periscope.component

import com.inductiveautomation.ignition.client.icons.VectorIcons
import com.mussonindustrial.ignition.embr.periscope.i18n
import java.awt.Color
import java.awt.Component
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer

class PaletteCellRenderer : DefaultTreeCellRenderer() {

    companion object {
        private val ORANGE = Color(194, 65, 12)
    }

    override fun getTreeCellRendererComponent(
        tree: JTree,
        value: Any?,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean,
    ): Component {
        val item = (value as? DefaultMutableTreeNode)?.userObject
        val label = (item as? PaletteFolder)?.name.orEmpty()
        super.getTreeCellRendererComponent(tree, label, selected, expanded, leaf, row, hasFocus)

        when (item) {
            is PaletteHint -> renderHint(item, selected)
            is PaletteSection -> renderSection(item)
            is PaletteVariant -> renderVariant(item)
            is ComponentInfo -> renderComponent(item, selected)
        }
        return this
    }

    private fun renderHint(item: PaletteHint, selected: Boolean) {
        text = item.text
        icon = null
        toolTipText = null
        if (!selected) {
            foreground = Color.GRAY
        }
    }

    private fun renderSection(item: PaletteSection) {
        text = item.name
        icon = VectorIcons.get(item.iconName)
        toolTipText = null
    }

    private fun renderVariant(item: PaletteVariant) {
        text = item.variant.name
        icon = ComponentIcons.get(item.info.id)
        toolTipText =
            tooltipOf(
                listOfNotNull("${item.info.name} - ${item.variant.name}", item.variant.description),
            )
    }

    private fun renderComponent(info: ComponentInfo, selected: Boolean) {
        val diagnostics = diagnosticsOf(info)
        text = if (diagnostics.isEmpty()) info.name else "${info.name}  (!)"
        icon = ComponentIcons.get(info.id)

        toolTipText =
            tooltipOf(listOfNotNull(info.definition.description, info.id, info.path) + diagnostics)

        if (diagnostics.isNotEmpty() && !selected) {
            foreground = ORANGE
        }
    }

    private fun tooltipOf(lines: List<String>): String {
        return lines.joinToString("<br>", "<html>", "</html>")
    }

    private fun diagnosticsOf(info: ComponentInfo): List<String> {
        val diagnostics = info.diagnostics.toMutableList()
        val icon = info.definition.icon
        if (icon != null && ComponentIcons.get(info.id).missing) {
            diagnostics += i18n("palette.error.iconNotFound", icon)
        }
        return diagnostics
    }
}
