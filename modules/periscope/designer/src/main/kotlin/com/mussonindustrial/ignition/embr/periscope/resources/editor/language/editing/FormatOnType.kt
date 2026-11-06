package com.mussonindustrial.ignition.embr.periscope.resources.editor.language.editing

import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.LanguageSession
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.applyEdits
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.SwingUtilities

class FormatOnType(private val session: LanguageSession) {

    private val textArea = session.textArea

    private val keyListener =
        object : KeyAdapter() {
            override fun keyTyped(e: KeyEvent) {
                val key = e.keyChar
                if (key in TRIGGERS) SwingUtilities.invokeLater { format(key) }
            }
        }

    fun install() {
        textArea.addKeyListener(keyListener)
    }

    fun uninstall() {
        textArea.removeKeyListener(keyListener)
    }

    private fun format(key: Char) {
        if (key == '\n' && !isNewLine()) return
        val caret = textArea.caretPosition
        session.request({ formatOnType(session.path, caret, key) }) { result ->
            val text = textArea.text
            val edits =
                result.edits.filter { text.substring(it.start, it.start + it.length) != it.newText }
            if (caret == textArea.caretPosition && edits.isNotEmpty()) textArea.applyEdits(edits)
        }
    }

    private fun isNewLine(): Boolean {
        val line = textArea.getLineOfOffset(textArea.caretPosition)
        val lineStart = textArea.getLineStartOffset(line)
        return line > 0 && textArea.getText(lineStart, textArea.caretPosition - lineStart).isBlank()
    }

    companion object {
        private const val TRIGGERS = ";}\n"
    }
}
