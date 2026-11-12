package com.mussonindustrial.ignition.embr.periscope.resources.editor.language

import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.classification.Classifications
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.classification.ClassifiedTokenMaker
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.classification.TokenKind
import com.mussonindustrial.ignition.embr.periscope.resources.language.ResourceFile
import com.mussonindustrial.ignition.embr.periscope.resources.language.TypeScriptLanguageService
import com.mussonindustrial.ignition.embr.periscope.resources.language.TypeScriptWorkspace
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import javax.swing.SwingUtilities
import javax.swing.Timer
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea

class LanguageSession(
    private val workspace: TypeScriptWorkspace,
    val path: String,
    val textArea: RSyntaxTextArea,
    val language: ScriptLanguage,
) {
    private val service: TypeScriptLanguageService = workspace.service

    var version = 0
        private set

    private var syncedVersion = -1
    private val tokenMaker = ClassifiedTokenMaker()
    private val refreshes = mutableListOf<Refresh>()
    private var installed = false
    private val workspaceListener: (String) -> Unit = { changed ->
        if (changed != path) SwingUtilities.invokeLater { restart { it.onOtherResources } }
    }

    private val documentListener =
        object : DocumentListener {
            override fun insertUpdate(event: DocumentEvent) {
                version++
                tokenMaker.classifications?.inserted(event.offset, event.length)
                restart { true }
            }

            override fun removeUpdate(event: DocumentEvent) {
                version++
                tokenMaker.classifications?.removed(event.offset, event.length)
                restart { true }
            }

            override fun changedUpdate(event: DocumentEvent) {}
        }

    init {
        refreshOnEdit(150, onOtherResources = true, ::classify)
    }

    fun install() {
        (textArea.document as RSyntaxDocument).setSyntaxStyle(tokenMaker)
        textArea.document.addDocumentListener(documentListener)
        workspace.addChangeListener(workspaceListener)
        installed = true
        refreshes.forEach { it.refresh() }
    }

    fun uninstall() {
        installed = false
        refreshes.forEach { it.timer.stop() }
        workspace.removeChangeListener(workspaceListener)
        textArea.document.removeDocumentListener(documentListener)
        workspace.close(path)
        syncedVersion = -1
    }

    fun refreshOnEdit(delay: Int, onOtherResources: Boolean = false, refresh: () -> Unit) {
        val timer = Timer(delay) { refresh() }.apply { isRepeats = false }
        refreshes += Refresh(timer, onOtherResources, refresh)
        if (installed) refresh()
    }

    private fun restart(which: (Refresh) -> Boolean) {
        refreshes.filter(which).forEach { it.timer.restart() }
    }

    private fun sync() {
        if (version == syncedVersion) return
        workspace.update(ResourceFile(path, language.kind, textArea.text))
        syncedVersion = version
    }

    fun <T> query(
        waitMs: Long = COMMAND_WAIT_MS,
        query: TypeScriptLanguageService.() -> CompletableFuture<T?>,
    ): T? {
        sync()
        val pending = service.query()
        return try {
            pending.get(waitMs, TimeUnit.MILLISECONDS)
        } catch (e: Exception) {
            pending.cancel(false)
            null
        }
    }

    fun <T> request(
        query: TypeScriptLanguageService.() -> CompletableFuture<T?>,
        apply: (T) -> Unit,
    ) {
        sync()
        val requested = version
        service.query().thenAccept { result ->
            SwingUtilities.invokeLater {
                if (result != null && requested == version) apply(result)
            }
        }
    }

    private fun classify() {
        request({ classifications(path) }) {
            tokenMaker.classifications =
                Classifications(it.tokens, it.kinds.map(TokenKind::tokenTypeOf))
            textArea.repaint()
        }
    }

    companion object {
        const val HOVER_WAIT_MS = 100L
        private const val COMMAND_WAIT_MS = 2000L
    }

    private class Refresh(
        val timer: Timer,
        val onOtherResources: Boolean,
        val refresh: () -> Unit,
    )
}
