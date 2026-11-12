package com.mussonindustrial.ignition.embr.periscope.resources.editor.language.completion

import com.inductiveautomation.ignition.client.icons.InteractiveVectorIcon
import com.inductiveautomation.ignition.client.icons.VectorIcons
import com.inductiveautomation.ignition.designer.gui.IconUtil
import javax.swing.Icon

object CompletionIcons {
    private val function: InteractiveVectorIcon = VectorIcons.getInteractive("expressions")
    private val variable: InteractiveVectorIcon = VectorIcons.getInteractive("variable")
    private val type: InteractiveVectorIcon = VectorIcons.getInteractive("class")
    private val property: InteractiveVectorIcon = VectorIcons.getInteractive("property")
    private val deprecated: InteractiveVectorIcon = VectorIcons.getInteractive("warning")
    private val module: Icon = IconUtil.getRootIconScaled("nav/script-package.png", 16)
    private val moduleSelected: Icon =
        IconUtil.getRootIconScaled("nav/script-package-selected.png", 16)

    fun of(kind: String, kindModifiers: String, selected: Boolean): Icon? {
        if ("deprecated" in kindModifiers) return deprecated.select(selected)
        return when (kind) {
            "function",
            "local function",
            "method",
            "constructor",
            "getter",
            "setter" -> function.select(selected)
            "var",
            "let",
            "const",
            "local var",
            "parameter",
            "alias",
            "enum member" -> variable.select(selected)
            "property",
            "JSX attribute" -> property.select(selected)
            "class",
            "local class",
            "interface",
            "type",
            "enum",
            "type parameter",
            "primitive type" -> type.select(selected)
            "module",
            "external module name",
            "directory",
            "script" -> if (selected) moduleSelected else module
            else -> null
        }
    }

    private fun InteractiveVectorIcon.select(selected: Boolean): Icon =
        if (selected) this.selected ?: this else this
}
