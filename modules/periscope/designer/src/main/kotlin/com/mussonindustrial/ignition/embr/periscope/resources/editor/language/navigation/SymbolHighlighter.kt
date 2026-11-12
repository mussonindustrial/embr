package com.mussonindustrial.ignition.embr.periscope.resources.editor.language.navigation

import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.LanguageSession
import javax.swing.Timer
import javax.swing.event.CaretListener
import org.fife.ui.rtextarea.SmartHighlightPainter

class SymbolHighlighter(private val session: LanguageSession) {

    private val textArea = session.textArea
    private val painter = SmartHighlightPainter(textArea.markOccurrencesColor)
    private val tags = mutableListOf<Any>()
    private val debounce = Timer(300) { highlight() }.apply { isRepeats = false }
    private val caretListener = CaretListener { debounce.restart() }

    fun install() {
        textArea.addCaretListener(caretListener)
    }

    fun uninstall() {
        debounce.stop()
        textArea.removeCaretListener(caretListener)
        clear()
    }

    private fun highlight() {
        clear()
        if (textArea.selectionStart != textArea.selectionEnd) return

        val caret = textArea.caretPosition
        session.request({ highlights(session.path, caret) }) { result ->
            if (caret != textArea.caretPosition || result.highlights.size < 2) return@request
            result.highlights.forEach {
                tags += textArea.highlighter.addHighlight(it.start, it.start + it.length, painter)
            }
        }
    }

    private fun clear() {
        tags.forEach { textArea.highlighter.removeHighlight(it) }
        tags.clear()
    }
}
