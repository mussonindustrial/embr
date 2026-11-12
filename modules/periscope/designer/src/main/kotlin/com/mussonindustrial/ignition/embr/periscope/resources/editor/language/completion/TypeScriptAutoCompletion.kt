package com.mussonindustrial.ignition.embr.periscope.resources.editor.language.completion

import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.applyEdits
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.shift
import com.mussonindustrial.ignition.embr.periscope.resources.language.TextEdit
import org.fife.ui.autocomplete.AutoCompletion
import org.fife.ui.autocomplete.Completion
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea

class TypeScriptAutoCompletion(private val provider: TypeScriptCompletionProvider) :
    AutoCompletion(provider) {

    init {
        setAutoActivationEnabled(true)
        setAutoActivationDelay(250)
        setAutoCompleteSingleChoices(false)
        setParameterAssistanceEnabled(false)
        setShowDescWindow(true)
    }

    override fun insertCompletion(completion: Completion, typedParamListStartChar: Boolean) {
        val typescript = completion as? TypeScriptCompletion
        if (typescript == null) {
            super.insertCompletion(completion, typedParamListStartChar)
            return
        }

        val textArea = textComponent as RSyntaxTextArea
        val edit = replacementEdit(typescript, textArea.caretPosition)
        val additional = typescript.additionalEdits()

        hidePopupWindow()
        textArea.applyEdits(additional + edit)
        textArea.caretPosition = additional.shift(edit.start) + edit.newText.length
    }

    private fun replacementEdit(completion: TypeScriptCompletion, caret: Int): TextEdit {
        val text = completion.replacementText
        val span = completion.entry.replacementSpan
        if (span != null) {
            return TextEdit(span.start, maxOf(span.length, caret - span.start), text)
        }
        val entered = provider.getAlreadyEnteredText(textComponent)
        return TextEdit(caret - entered.length, entered.length, text)
    }
}
