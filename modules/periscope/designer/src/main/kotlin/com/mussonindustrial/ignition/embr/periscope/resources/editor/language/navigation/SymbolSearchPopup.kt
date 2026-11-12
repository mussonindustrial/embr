package com.mussonindustrial.ignition.embr.periscope.resources.editor.language.navigation

import com.inductiveautomation.ignition.client.IgnitionLookAndFeel
import com.mussonindustrial.ignition.embr.periscope.resources.editor.SimpleDocumentListener
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.Html
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.LanguageSession
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.completion.CompletionIcons
import com.mussonindustrial.ignition.embr.periscope.resources.language.Location
import com.mussonindustrial.ignition.embr.periscope.resources.language.SymbolItem
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Point
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextField
import javax.swing.Timer

class SymbolSearchPopup(private val session: LanguageSession, navigate: (Location) -> Unit) {

    private val field = JTextField().apply { putClientProperty(PLACEHOLDER, "Go to symbol") }
    private val popup =
        ListPopup(
            ::label,
            { symbol, selected -> CompletionIcons.of(symbol.kind, "", selected) },
        ) {
            navigate(it.location)
        }
    private val debounce = Timer(100) { search() }.apply { isRepeats = false }

    init {
        popup.list.visibleRowCount = 12
        field.document.addDocumentListener(SimpleDocumentListener { debounce.restart() })
        field.addKeyListener(popup.keys)
    }

    fun show(owner: JComponent) {
        val content =
            JPanel(BorderLayout(0, 4)).apply {
                border =
                    BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(IgnitionLookAndFeel.Colors.Base500),
                        BorderFactory.createEmptyBorder(6, 6, 6, 6),
                    )
                add(field, BorderLayout.NORTH)
                add(JScrollPane(popup.list), BorderLayout.CENTER)
            }
        popup.open(owner, content, field) {
            size = Dimension(WIDTH, HEIGHT)
            location = topCenterOf(owner)
        }
        field.selectAll()
    }

    private fun topCenterOf(owner: JComponent): Point {
        val origin = owner.locationOnScreen
        val x = origin.x + (owner.width - WIDTH) / 2
        return Point(x.coerceAtLeast(origin.x), origin.y + TOP_GAP)
    }

    private fun search() {
        val query = field.text.trim()
        if (query.isEmpty()) {
            popup.show(emptyList())
            return
        }
        session.request({ symbols(query) }) { result ->
            if (field.text.trim() == query) popup.show(result.symbols)
        }
    }

    private fun label(symbol: SymbolItem, detailColor: String): String {
        val detail =
            listOf(symbol.containerName, "${symbol.location.path}:${symbol.location.line}")
                .filter { it.isNotEmpty() }
                .joinToString(" · ")
        val name = Html.escape(symbol.name)
        return "<html><b>$name</b>&nbsp;&nbsp;<font color='$detailColor'>${Html.escape(detail)}</font>"
    }

    companion object {
        private const val WIDTH = 560
        private const val HEIGHT = 320
        private const val TOP_GAP = 24
        private const val PLACEHOLDER = "JTextField.placeholderText"
    }
}
