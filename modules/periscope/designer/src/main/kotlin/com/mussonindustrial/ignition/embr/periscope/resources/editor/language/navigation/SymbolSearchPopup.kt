package com.mussonindustrial.ignition.embr.periscope.resources.editor.language.navigation

import com.inductiveautomation.ignition.client.IgnitionLookAndFeel
import com.inductiveautomation.ignition.client.IgnitionLookAndFeel.Colors.DisabledForeground
import com.mussonindustrial.ignition.embr.periscope.resources.editor.SimpleDocumentListener
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.Html
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.LanguageSession
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.completion.CompletionIcons
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.popupDialog
import com.mussonindustrial.ignition.embr.periscope.resources.language.Location
import com.mussonindustrial.ignition.embr.periscope.resources.language.SymbolItem
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.Point
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.DefaultListCellRenderer
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextField
import javax.swing.ListSelectionModel
import javax.swing.Timer

class SymbolSearchPopup(
    private val session: LanguageSession,
    private val navigate: (Location) -> Unit,
) {

    private val field = JTextField().apply { putClientProperty(PLACEHOLDER, "Go to symbol") }
    private val list =
        JList<SymbolItem>().apply {
            selectionMode = ListSelectionModel.SINGLE_SELECTION
            visibleRowCount = 12
            cellRenderer = SymbolRenderer()
        }
    private val debounce = Timer(100) { search() }.apply { isRepeats = false }
    private var dialog: JDialog? = null

    init {
        field.document.addDocumentListener(SimpleDocumentListener { debounce.restart() })
        field.addKeyListener(
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

    fun show(owner: JComponent) {
        hide()
        val content =
            JPanel(BorderLayout(0, 4)).apply {
                border =
                    BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(IgnitionLookAndFeel.Colors.Base500),
                        BorderFactory.createEmptyBorder(6, 6, 6, 6),
                    )
                add(field, BorderLayout.NORTH)
                add(JScrollPane(list), BorderLayout.CENTER)
            }
        dialog =
            popupDialog(owner, content).apply {
                size = Dimension(WIDTH, HEIGHT)
                location = topCenterOf(owner)
                isVisible = true
            }
        field.selectAll()
        field.requestFocusInWindow()
    }

    private fun topCenterOf(owner: JComponent): Point {
        val origin = owner.locationOnScreen
        val x = origin.x + (owner.width - WIDTH) / 2
        return Point(x.coerceAtLeast(origin.x), origin.y + TOP_GAP)
    }

    private fun hide() {
        dialog?.dispose()
        dialog = null
    }

    private fun search() {
        val query = field.text.trim()
        if (query.isEmpty()) {
            list.setListData(emptyArray())
            return
        }
        session.request({ symbols(query) }) { result ->
            if (field.text.trim() != query) return@request
            list.setListData(result.symbols.toTypedArray())
            list.selectedIndex = 0
        }
    }

    private fun move(delta: Int) {
        val size = list.model.size
        if (size == 0) return
        list.selectedIndex = (list.selectedIndex + delta).coerceIn(0, size - 1)
        list.ensureIndexIsVisible(list.selectedIndex)
    }

    private fun choose() {
        val symbol = list.selectedValue ?: return
        hide()
        navigate(symbol.location)
    }

    private class SymbolRenderer : DefaultListCellRenderer() {
        override fun getListCellRendererComponent(
            list: JList<*>,
            value: Any?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean,
        ): Component {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
            val symbol = value as? SymbolItem ?: return this
            val detailColor = if (isSelected) foreground else DisabledForeground
            text = label(symbol, detailColor)
            icon = CompletionIcons.of(symbol.kind, "", isSelected)
            border = BorderFactory.createEmptyBorder(3, 4, 3, 4)
            return this
        }

        private fun label(symbol: SymbolItem, detailColor: Color): String {
            val detail =
                listOf(symbol.containerName, "${symbol.location.path}:${symbol.location.line}")
                    .filter { it.isNotEmpty() }
                    .joinToString(" · ")
            val name = Html.escape(symbol.name)
            val color = Html.hex(detailColor)
            return "<html><b>$name</b>&nbsp;&nbsp;<font color='$color'>${Html.escape(detail)}</font>"
        }
    }

    companion object {
        private const val WIDTH = 560
        private const val HEIGHT = 320
        private const val TOP_GAP = 24
        private const val PLACEHOLDER = "JTextField.placeholderText"
    }
}
