package com.mussonindustrial.ignition.embr.periscope.resources.editor.language.hints

import com.mussonindustrial.ignition.embr.periscope.resources.editor.SimpleDocumentListener
import com.mussonindustrial.ignition.embr.periscope.resources.editor.components.LineHint
import com.mussonindustrial.ignition.embr.periscope.resources.editor.components.ScriptTextArea
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.LanguageSession
import com.mussonindustrial.ignition.embr.periscope.resources.language.InlayHint
import java.util.prefs.Preferences
import javax.swing.Timer

class InlayHints(private val session: LanguageSession, private val textArea: ScriptTextArea) {

    var enabled = PREFERENCES.getBoolean(SHOW_INLAY_HINTS, true)
        set(value) {
            field = value
            PREFERENCES.putBoolean(SHOW_INLAY_HINTS, value)
            if (value) refresh() else textArea.lineHints = emptyList()
        }

    private val debounce = Timer(500) { refresh() }.apply { isRepeats = false }
    private val documentListener = SimpleDocumentListener { debounce.restart() }

    fun install() {
        textArea.document.addDocumentListener(documentListener)
        refresh()
    }

    fun uninstall() {
        debounce.stop()
        textArea.document.removeDocumentListener(documentListener)
        textArea.lineHints = emptyList()
    }

    private fun refresh() {
        if (!enabled) return
        session.request({ inlayHints(session.path) }) { result ->
            val text = textArea.text
            textArea.lineHints =
                result.hints.filter { it.position <= text.length }.map { toLineHint(text, it) }
        }
    }

    private fun toLineHint(text: String, hint: InlayHint): LineHint {
        val label =
            if (hint.kind == InlayHint.PARAMETER) "${hint.text} ${argumentAt(text, hint.position)}"
            else typeLabel(text, hint)
        return LineHint(textArea.document.createPosition(hint.position), label)
    }

    private fun typeLabel(text: String, hint: InlayHint): String {
        val name = text.substring(0, hint.position).takeLastWhile { it.isJavaIdentifierPart() }
        val type = hint.text.removePrefix(":").trim()
        return if (name.isEmpty()) "→ $type" else "$name: $type"
    }

    private fun argumentAt(text: String, start: Int): String {
        var depth = 0
        var end = start
        while (end < text.length && end - start < MAX_ARGUMENT_LENGTH) {
            val char = text[end]
            if (char == '\n') break
            if (char in "([{") depth++
            if (char in ")]}" && depth-- == 0) break
            if (char == ',' && depth == 0) break
            end++
        }
        val argument = text.substring(start, end).trim()
        return if (end - start >= MAX_ARGUMENT_LENGTH) "$argument…" else argument
    }

    companion object {
        private const val MAX_ARGUMENT_LENGTH = 24
        private const val SHOW_INLAY_HINTS = "showInlayHints"
        private val PREFERENCES = Preferences.userNodeForPackage(InlayHints::class.java)
    }
}
