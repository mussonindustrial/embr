package com.mussonindustrial.ignition.embr.periscope.resources.editor.language.navigation

import com.inductiveautomation.ignition.client.IgnitionLookAndFeel
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.Html
import com.mussonindustrial.ignition.embr.periscope.resources.language.Location
import java.awt.Dimension
import java.awt.Point
import javax.swing.BorderFactory
import javax.swing.JScrollPane
import javax.swing.SwingUtilities
import org.fife.ui.rtextarea.RTextArea

class ReferencesPopup(private val path: String, navigate: (Location) -> Unit) {

    private val popup = ListPopup(::label, onChoose = navigate)

    init {
        popup.list.addKeyListener(popup.keys)
    }

    fun show(textArea: RTextArea, references: List<Location>) {
        val caret = textArea.modelToView2D(textArea.caretPosition)
        if (references.isEmpty() || caret == null) return

        popup.show(references)
        popup.list.visibleRowCount = references.size.coerceAtMost(MAX_ROWS)
        val content =
            JScrollPane(popup.list).apply {
                border = BorderFactory.createLineBorder(IgnitionLookAndFeel.Colors.Base500)
            }
        val belowCaret = Point(caret.x.toInt(), (caret.y + caret.height).toInt())
        SwingUtilities.convertPointToScreen(belowCaret, textArea)
        popup.open(textArea, content, popup.list) {
            pack()
            size = Dimension(WIDTH, height)
            location = belowCaret
        }
    }

    private fun label(reference: Location, detailColor: String): String {
        val code = Html.escape(reference.lineText.trim())
        val file = if (reference.path == path) "" else reference.path
        val place = Html.escape("$file:${reference.line}")
        return "<html><span style='font-family: monospace;'>$code</span>" +
            "&nbsp;&nbsp;<font color='$detailColor'>$place</font>"
    }

    companion object {
        private const val WIDTH = 560
        private const val MAX_ROWS = 12
    }
}
