package com.mussonindustrial.ignition.embr.periscope.resources.editor.language.completion

import com.inductiveautomation.ignition.client.IgnitionLookAndFeel
import java.awt.Component
import java.awt.Font
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.ListCellRenderer
import javax.swing.SwingConstants
import net.miginfocom.swing.MigLayout

class TypeScriptCompletionRenderer : JPanel(MigLayout("ins 0, fill")), ListCellRenderer<Any> {

    private val name = JLabel().apply { font = Font(Font.MONOSPACED, Font.PLAIN, 12) }
    private val detail =
        JLabel().apply {
            font = Font(Font.MONOSPACED, Font.PLAIN, 12)
            horizontalAlignment = SwingConstants.RIGHT
        }

    init {
        add(name, "w 0::100%, gap 16")
        add(detail, "right")
    }

    override fun getListCellRendererComponent(
        list: JList<*>,
        value: Any?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean,
    ): Component {
        val completion = value as TypeScriptCompletion
        val entry = completion.entry

        name.text = entry.name
        name.icon = CompletionIcons.of(entry.kind, entry.kindModifiers, isSelected)
        detail.text = entry.source ?: ""

        background = if (isSelected) list.selectionBackground else list.background
        name.foreground = if (isSelected) list.selectionForeground else list.foreground
        detail.foreground =
            if (isSelected) list.selectionForeground
            else IgnitionLookAndFeel.Colors.DisabledForeground
        return this
    }
}
