package com.mussonindustrial.ignition.embr.periscope.resources.editor.language.navigation

import com.inductiveautomation.ignition.client.IgnitionLookAndFeel.Colors.DisabledForeground
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.Html
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.popupDialog
import java.awt.Component
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.Vector
import javax.swing.BorderFactory
import javax.swing.DefaultListCellRenderer
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JList
import javax.swing.ListSelectionModel

class ListPopup<T>(
    private val label: (item: T, detailColor: String) -> String,
    private val icon: (item: T, selected: Boolean) -> Icon? = { _, _ -> null },
    private val onChoose: (T) -> Unit,
) {
    val list =
        JList<T>().apply {
            selectionMode = ListSelectionModel.SINGLE_SELECTION
            cellRenderer = Renderer()
            addMouseListener(
                object : MouseAdapter() {
                    override fun mouseClicked(e: MouseEvent) {
                        if (e.clickCount == 2) choose()
                    }
                },
            )
        }

    val keys =
        object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                when (e.keyCode) {
                    KeyEvent.VK_DOWN -> move(1)
                    KeyEvent.VK_UP -> move(-1)
                    KeyEvent.VK_ENTER -> choose()
                    KeyEvent.VK_ESCAPE -> hide()
                    else -> return
                }
                e.consume()
            }
        }

    private var dialog: JDialog? = null

    fun show(items: List<T>) {
        list.setListData(Vector(items))
        list.selectedIndex = 0
    }

    fun open(owner: JComponent, content: JComponent, focus: JComponent, place: JDialog.() -> Unit) {
        hide()
        dialog =
            popupDialog(owner, content).apply {
                place()
                isVisible = true
            }
        focus.requestFocusInWindow()
    }

    private fun hide() {
        dialog?.dispose()
        dialog = null
    }

    private fun move(delta: Int) {
        val size = list.model.size
        if (size == 0) return
        list.selectedIndex = (list.selectedIndex + delta).coerceIn(0, size - 1)
        list.ensureIndexIsVisible(list.selectedIndex)
    }

    private fun choose() {
        val item = list.selectedValue ?: return
        hide()
        onChoose(item)
    }

    private inner class Renderer : DefaultListCellRenderer() {
        override fun getListCellRendererComponent(
            list: JList<*>,
            value: Any?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean,
        ): Component {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
            @Suppress("UNCHECKED_CAST") val item = value as T
            text = label(item, Html.hex(if (isSelected) foreground else DisabledForeground))
            icon = icon(item, isSelected)
            border = BorderFactory.createEmptyBorder(3, 6, 3, 6)
            return this
        }
    }
}
