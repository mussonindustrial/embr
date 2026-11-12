package com.mussonindustrial.ignition.embr.periscope.resources.editor.language

import com.mussonindustrial.ignition.embr.periscope.resources.editor.components.ScriptEditor
import com.mussonindustrial.ignition.embr.periscope.resources.editor.components.ScriptEditor.Companion.MENU_SHORTCUT
import com.mussonindustrial.ignition.embr.periscope.resources.editor.components.ScriptEditor.Companion.menuItem
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.hints.InlayHints
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.hints.SignatureHelpPopup
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.navigation.ReferencesPopup
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.navigation.SymbolSearchPopup
import com.mussonindustrial.ignition.embr.periscope.resources.language.FileEdits
import com.mussonindustrial.ignition.embr.periscope.resources.language.Location
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import javax.swing.JCheckBoxMenuItem
import javax.swing.JMenuItem
import javax.swing.JPopupMenu
import javax.swing.KeyStroke

class LanguageCommands(
    private val session: LanguageSession,
    private val editor: ScriptEditor,
    private val goTo: (Location) -> Unit,
    private val edit: (FileEdits) -> Unit,
    searchesSymbols: Boolean,
    signatureHelp: SignatureHelpPopup,
    inlayHints: InlayHints,
) {
    private val textArea = editor.textArea
    private val popup = textArea.popupMenu
    private val references = ReferencesPopup(session.path, goTo)

    init {
        popup?.addSeparator()
        command("Go to Definition", DEFINITION_KEY, ::goToDefinition)
        command("Find References", REFERENCES_KEY, ::findReferences)
        if (searchesSymbols) {
            val symbolSearch = SymbolSearchPopup(session, goTo)
            command("Go to Symbol...", SYMBOL_KEY) { symbolSearch.show(editor) }
        }
        command("Quick Fix...", QUICK_FIX_KEY, ::showQuickFixes)
        if (session.language == ScriptLanguage.TYPESCRIPT) {
            command("Organize Imports", ORGANIZE_IMPORTS_KEY, ::organizeImports)
        }
        editor.bindKey(SIGNATURE_HELP_KEY) { signatureHelp.show() }
        popup?.add(
            JCheckBoxMenuItem("Show Inlay Hints", inlayHints.enabled).apply {
                addActionListener { inlayHints.enabled = isSelected }
            },
        )
    }

    private fun command(text: String, key: KeyStroke, action: () -> Unit) {
        editor.bindKey(key) { action() }
        popup?.add(menuItem(text, key, action))
    }

    private fun goToDefinition() {
        val caret = textArea.caretPosition
        session.query { definition(session.path, caret) }?.target?.let(goTo)
    }

    private fun findReferences() {
        val caret = textArea.caretPosition
        val result = session.query { references(session.path, caret) } ?: return
        references.show(textArea, result.references)
    }

    private fun showQuickFixes() {
        val start = textArea.selectionStart
        val end = textArea.selectionEnd
        val actions = session.query { codeFixes(session.path, start, end) }?.actions.orEmpty()
        val menu = JPopupMenu()
        if (actions.isEmpty()) {
            menu.add(JMenuItem("No quick fixes available.").apply { isEnabled = false })
        }
        actions.forEach { action ->
            menu.add(
                JMenuItem(action.description).apply {
                    addActionListener { action.changes.forEach(edit) }
                },
            )
        }
        val caret = textArea.modelToView2D(start) ?: return
        menu.show(textArea, caret.x.toInt(), (caret.y + caret.height).toInt())
    }

    private fun organizeImports() {
        val edits = session.query { organizeImports(session.path) }?.edits ?: return
        textArea.applyEdits(edits)
    }

    companion object {
        private val DEFINITION_KEY: KeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0)
        private val REFERENCES_KEY: KeyStroke =
            KeyStroke.getKeyStroke(KeyEvent.VK_F12, InputEvent.SHIFT_DOWN_MASK)
        private val SYMBOL_KEY: KeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_T, MENU_SHORTCUT)
        private val QUICK_FIX_KEY: KeyStroke =
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_DOWN_MASK)
        private val ORGANIZE_IMPORTS_KEY: KeyStroke =
            KeyStroke.getKeyStroke(
                KeyEvent.VK_O,
                InputEvent.SHIFT_DOWN_MASK or InputEvent.ALT_DOWN_MASK,
            )
        private val SIGNATURE_HELP_KEY: KeyStroke =
            KeyStroke.getKeyStroke(
                KeyEvent.VK_SPACE,
                InputEvent.CTRL_DOWN_MASK or InputEvent.SHIFT_DOWN_MASK,
            )
    }
}
