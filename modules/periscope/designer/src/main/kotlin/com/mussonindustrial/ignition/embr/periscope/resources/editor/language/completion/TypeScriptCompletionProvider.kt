package com.mussonindustrial.ignition.embr.periscope.resources.editor.language.completion

import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.LanguageSession
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.closesString
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.lineBeforeCaret
import java.awt.Point
import javax.swing.text.JTextComponent
import org.fife.ui.autocomplete.Completion
import org.fife.ui.autocomplete.CompletionProviderBase
import org.fife.ui.autocomplete.ParameterizedCompletion

class TypeScriptCompletionProvider(val session: LanguageSession) : CompletionProviderBase() {

    init {
        setAutoActivationRules(true, ".'\"/@<")
        setListCellRenderer(TypeScriptCompletionRenderer())
    }

    override fun getAlreadyEnteredText(comp: JTextComponent): String {
        return lineBeforeCaret(comp).takeLastWhile { it.isJavaIdentifierPart() }
    }

    override fun isAutoActivateOkay(comp: JTextComponent): Boolean {
        return super.isAutoActivateOkay(comp) && !closesString(comp)
    }

    override fun getCompletions(comp: JTextComponent): List<Completion> = getCompletionsImpl(comp)

    override fun getCompletionsImpl(comp: JTextComponent): List<Completion> {
        if (closesString(comp)) return emptyList()
        val offset = comp.caretPosition
        val prefix = getAlreadyEnteredText(comp)
        val result =
            session.query { completions(session.path, offset, prefix) } ?: return emptyList()
        return result.entries.map { TypeScriptCompletion(this, it, offset) }
    }

    override fun getCompletionsAt(comp: JTextComponent, p: Point): List<Completion> = emptyList()

    override fun getParameterizedCompletions(tc: JTextComponent): List<ParameterizedCompletion> =
        emptyList()
}
