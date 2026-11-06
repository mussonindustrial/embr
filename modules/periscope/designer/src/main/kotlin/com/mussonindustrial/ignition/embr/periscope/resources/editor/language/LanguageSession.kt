package com.mussonindustrial.ignition.embr.periscope.resources.editor.language

import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.classification.Classifications
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.classification.ClassifiedTokenMaker
import com.mussonindustrial.ignition.embr.periscope.resources.language.ClientResourceLanguageProject
import com.mussonindustrial.ignition.embr.periscope.resources.language.ResourceFile
import com.mussonindustrial.ignition.embr.periscope.resources.language.TypeScriptLanguageService
import javax.swing.SwingUtilities
import javax.swing.Timer
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea

class LanguageSession(
    private val project: ClientResourceLanguageProject,
    val path: String,
    val textArea: RSyntaxTextArea,
    val language: ScriptLanguage,
) {
    val service: TypeScriptLanguageService = project.service

    var version = 0
        private set

    private var syncedVersion = -1
    private val tokenMaker = ClassifiedTokenMaker()
    private val classifyDebounce = Timer(150) { classify() }.apply { isRepeats = false }

    private val documentListener =
        object : DocumentListener {
            override fun insertUpdate(event: DocumentEvent) {
                version++
                tokenMaker.classifications?.inserted(event.offset, event.length)
                classifyDebounce.restart()
            }

            override fun removeUpdate(event: DocumentEvent) {
                version++
                tokenMaker.classifications?.removed(event.offset, event.length)
                classifyDebounce.restart()
            }

            override fun changedUpdate(event: DocumentEvent) {}
        }

    fun install() {
        (textArea.document as RSyntaxDocument).setSyntaxStyle(tokenMaker)
        textArea.document.addDocumentListener(documentListener)
        classify()
    }

    fun uninstall() {
        classifyDebounce.stop()
        textArea.document.removeDocumentListener(documentListener)
        project.close(path)
    }

    fun sync() {
        if (version == syncedVersion) return
        project.update(ResourceFile(path, language.kind, textArea.text))
        syncedVersion = version
    }

    fun <T> request(query: TypeScriptLanguageService.() -> T?, apply: (T) -> Unit) {
        sync()
        val requested = version
        service
            .background { service.query() }
            .thenAccept { result ->
                SwingUtilities.invokeLater {
                    if (result != null && requested == version) apply(result)
                }
            }
    }

    private fun classify() {
        request({ classifications(path) }) {
            tokenMaker.classifications = Classifications(it.tokens)
            textArea.repaint()
        }
    }
}
