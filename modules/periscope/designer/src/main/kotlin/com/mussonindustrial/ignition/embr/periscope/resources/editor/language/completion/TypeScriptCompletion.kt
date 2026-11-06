package com.mussonindustrial.ignition.embr.periscope.resources.editor.language.completion

import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.Html
import com.mussonindustrial.ignition.embr.periscope.resources.language.CompletionDetailsResult
import com.mussonindustrial.ignition.embr.periscope.resources.language.CompletionEntry
import com.mussonindustrial.ignition.embr.periscope.resources.language.TextEdit
import org.fife.ui.autocomplete.BasicCompletion

class TypeScriptCompletion(
    private val provider: TypeScriptCompletionProvider,
    val entry: CompletionEntry,
    private val offset: Int,
) : BasicCompletion(provider, entry.insertText ?: entry.name, entry.source) {

    private val details: CompletionDetailsResult? by lazy {
        provider.session.service.completionDetails(provider.session.path, offset, entry)
    }

    override fun getInputText(): String = entry.name

    override fun getSummary(): String? {
        val details = details ?: return null
        return Html.description(details.display, details.documentationHtml)
    }

    override fun getToolTipText(): String? = details?.display

    fun additionalEdits(): List<TextEdit> {
        if (!entry.hasAction) return emptyList()
        return details
            ?.codeActions
            .orEmpty()
            .flatMap { it.changes }
            .filter { it.path == provider.session.path }
            .flatMap { it.edits }
    }
}
