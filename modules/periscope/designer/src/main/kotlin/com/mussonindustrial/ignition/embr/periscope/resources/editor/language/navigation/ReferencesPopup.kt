package com.mussonindustrial.ignition.embr.periscope.resources.editor.language.navigation

import com.inductiveautomation.ignition.client.IgnitionLookAndFeel
import com.inductiveautomation.ignition.client.IgnitionLookAndFeel.Colors.DisabledForeground
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.Html
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.popupDialog
import com.mussonindustrial.ignition.embr.periscope.resources.language.Location
import java.awt.Component
import java.awt.Dimension
import java.awt.Point
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.DefaultListCellRenderer
import javax.swing.JDialog
import javax.swing.JList
import javax.swing.JScrollPane
import javax.swing.ListSelectionModel
import javax.swing.SwingUtilities
import org.fife.ui.rtextarea.RTextArea

class ReferencesPopup(private val path: String, private val navigate: (Location) -> Unit) {

    private val list =
        JList<Location>().apply {
            selectionMode = ListSelectionModel.SINGLE_SELECTION
            cellRenderer = ReferenceRenderer(path)
        }
    private var dialog: JDialog? = null

    init {
        list.addKeyListener(
            object : KeyAdapter() {
                override fun keyPressed(e: KeyEvent) {
                    when (e.keyCode) {
                        KeyEvent.VK_ENTER -> choose()
                        KeyEvent.VK_ESCAPE -> hide()
                        else -> return
                    }
                    e.consume()
                }
            },
        )
        list.addMouseListener(
            object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (e.clickCount == 2) choose()
                }
            },
        )
    }

    fun show(textArea: RTextArea, references: List<Location>) {
        hide()
        if (references.isEmpty()) return
        list.setListData(references.toTypedArray())
        list.selectedIndex = 0
        list.visibleRowCount = references.size.coerceAtMost(MAX_ROWS)

        val content =
            JScrollPane(list).apply {
                border = BorderFactory.createLineBorder(IgnitionLookAndFeel.Colors.Base500)
            }
        dialog =
            popupDialog(textArea, content).apply {
                pack()
                size = Dimension(WIDTH, height)
                location = belowCaret(textArea)
                isVisible = true
            }
        list.requestFocusInWindow()
    }

    private fun belowCaret(textArea: RTextArea): Point {
        val caret = textArea.modelToView2D(textArea.caretPosition)
        val location = Point(caret.x.toInt(), (caret.y + caret.height).toInt())
        SwingUtilities.convertPointToScreen(location, textArea)
        return location
    }

    private fun hide() {
        dialog?.dispose()
        dialog = null
    }

    private fun choose() {
        val reference = list.selectedValue ?: return
        hide()
        navigate(reference)
    }

    private class ReferenceRenderer(private val path: String) : DefaultListCellRenderer() {
        override fun getListCellRendererComponent(
            list: JList<*>,
            value: Any?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean,
        ): Component {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
            val reference = value as? Location ?: return this
            val detailColor = Html.hex(if (isSelected) foreground else DisabledForeground)
            val code = Html.escape(reference.lineText.trim())
            val file = if (reference.path == path) "" else reference.path
            val place = Html.escape("$file:${reference.line}")
            text =
                "<html><span style='font-family: monospace;'>$code</span>" +
                    "&nbsp;&nbsp;<font color='$detailColor'>$place</font>"
            border = BorderFactory.createEmptyBorder(3, 6, 3, 6)
            return this
        }
    }

    companion object {
        private const val WIDTH = 560
        private const val MAX_ROWS = 12
    }
}
