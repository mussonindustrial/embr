package com.mussonindustrial.ignition.embr.periscope.resources.editor.language

import com.mussonindustrial.ignition.embr.periscope.resources.editor.components.ScriptEditor
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.completion.TypeScriptAutoCompletion
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.completion.TypeScriptCompletionProvider
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.editing.FormatOnType
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.editing.ImportBraceCloser
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.editing.JsxTagCloser
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.editing.LinkedTagEditor
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.hints.InlayHints
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.hints.QuickInfoToolTipSupplier
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.hints.SignatureHelpPopup
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.navigation.DefinitionLinkGenerator
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.navigation.ReferencesPopup
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.navigation.SymbolHighlighter
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.navigation.SymbolSearchPopup
import com.mussonindustrial.ignition.embr.periscope.resources.language.ClientResourceLanguageProject
import com.mussonindustrial.ignition.embr.periscope.resources.language.FileEdits
import com.mussonindustrial.ignition.embr.periscope.resources.language.Location

class LanguageFeatures(
    project: ClientResourceLanguageProject,
    private val editor: ScriptEditor,
    path: String,
    language: ScriptLanguage,
    private val navigate: ((Location) -> Unit)? = null,
    private val edit: ((FileEdits) -> Unit)? = null,
) {
    private val textArea = editor.textArea
    val session = LanguageSession(project, path, textArea, language)

    private val autoCompletion = TypeScriptAutoCompletion(TypeScriptCompletionProvider(session))
    private val symbolHighlighter = SymbolHighlighter(session)
    private val toolTipSupplier = QuickInfoToolTipSupplier(session)
    private val linkGenerator = DefinitionLinkGenerator(session, ::goTo)
    private val signatureHelp = SignatureHelpPopup(session)
    private val inlayHints = InlayHints(session, textArea)
    private val jsxTagCloser = JsxTagCloser(session)
    private val importBraceCloser = ImportBraceCloser(textArea)
    private val formatOnType = FormatOnType(session)
    private val linkedTags = LinkedTagEditor(session)

    init {
        LanguageCommands(
            session,
            editor,
            goTo = ::goTo,
            edit = ::applyEdits,
            references = ReferencesPopup(session.path, ::goTo),
            symbolSearch = navigate?.let { SymbolSearchPopup(session, ::goTo) },
            signatureHelp = signatureHelp,
            inlayHints = inlayHints,
        )
    }

    fun install() {
        session.install()
        autoCompletion.install(textArea)
        symbolHighlighter.install()
        signatureHelp.install()
        inlayHints.install()
        jsxTagCloser.install()
        importBraceCloser.install()
        formatOnType.install()
        linkedTags.install()
        textArea.toolTipSupplier = toolTipSupplier
        textArea.linkGenerator = linkGenerator
    }

    fun uninstall() {
        textArea.linkGenerator = null
        textArea.toolTipSupplier = null
        linkedTags.uninstall()
        formatOnType.uninstall()
        importBraceCloser.uninstall()
        jsxTagCloser.uninstall()
        inlayHints.uninstall()
        signatureHelp.uninstall()
        symbolHighlighter.uninstall()
        autoCompletion.uninstall()
        session.uninstall()
    }

    private fun goTo(location: Location) {
        val path = location.path ?: return
        if (path == session.path) editor.reveal(location) else navigate?.invoke(location)
    }

    private fun applyEdits(changes: FileEdits) {
        if (changes.path == session.path) textArea.applyEdits(changes.edits)
        else edit?.invoke(changes)
    }
}
