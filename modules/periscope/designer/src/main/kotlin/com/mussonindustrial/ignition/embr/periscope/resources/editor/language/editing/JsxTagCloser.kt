package com.mussonindustrial.ignition.embr.periscope.resources.editor.language.editing

import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.LanguageSession
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.SwingUtilities

class JsxTagCloser(private val session: LanguageSession) {

    private val textArea = session.textArea

    private val keyListener =
        object : KeyAdapter() {
            override fun keyTyped(e: KeyEvent) {
                if (e.keyChar == '>') SwingUtilities.invokeLater { close() }
            }
        }

    fun install() {
        textArea.addKeyListener(keyListener)
    }

    fun uninstall() {
        textArea.removeKeyListener(keyListener)
    }

    private fun close() {
        val caret = textArea.caretPosition
        session.request({ jsxClosingTag(session.path, caret) }) { tag ->
            if (textArea.caretPosition != caret) return@request
            textArea.document.insertString(caret, tag.newText, null)
            textArea.caretPosition = caret
        }
    }
}
