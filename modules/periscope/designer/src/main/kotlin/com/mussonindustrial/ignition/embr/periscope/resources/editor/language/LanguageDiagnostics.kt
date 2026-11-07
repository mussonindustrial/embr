package com.mussonindustrial.ignition.embr.periscope.resources.editor.language

import com.mussonindustrial.ignition.embr.periscope.resources.compiler.Diagnostic
import com.mussonindustrial.ignition.embr.periscope.resources.editor.SimpleDocumentListener
import javax.swing.Timer

class LanguageDiagnostics(
    private val session: LanguageSession,
    private val show: (List<Diagnostic>) -> Unit,
) {

    private val textArea = session.textArea
    private val debounce = Timer(400) { refresh() }.apply { isRepeats = false }
    private val documentListener = SimpleDocumentListener { debounce.restart() }

    fun install() {
        textArea.document.addDocumentListener(documentListener)
        refresh()
    }

    fun uninstall() {
        debounce.stop()
        textArea.document.removeDocumentListener(documentListener)
        show(emptyList())
    }

    private fun refresh() {
        session.request({ diagnostics(session.path) }) { show(it.diagnostics) }
    }
}
