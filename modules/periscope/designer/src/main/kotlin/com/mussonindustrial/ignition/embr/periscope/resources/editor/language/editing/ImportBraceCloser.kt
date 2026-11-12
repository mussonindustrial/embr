package com.mussonindustrial.ignition.embr.periscope.resources.editor.language.editing

import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.lineBeforeCaret
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.SwingUtilities
import javax.swing.text.JTextComponent

class ImportBraceCloser(private val textArea: JTextComponent) {

    private val keyListener =
        object : KeyAdapter() {
            override fun keyTyped(e: KeyEvent) {
                if (!isImportLine()) return
                when (e.keyChar) {
                    '{' -> SwingUtilities.invokeLater { close() }
                    '}' -> if (nextChar() == '}') skipClosing(e)
                }
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
        textArea.document.insertString(caret, "}", null)
        textArea.caretPosition = caret
    }

    private fun skipClosing(e: KeyEvent) {
        e.consume()
        textArea.caretPosition += 1
    }

    private fun isImportLine(): Boolean {
        val line = lineBeforeCaret(textArea).trimStart()
        return line.startsWith("import") || line.startsWith("export")
    }

    private fun nextChar(): Char? {
        val caret = textArea.caretPosition
        if (caret >= textArea.document.length) return null
        return textArea.document.getText(caret, 1).single()
    }
}
