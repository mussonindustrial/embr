package com.mussonindustrial.ignition.embr.periscope.resources.editor.components

import com.mussonindustrial.ignition.embr.periscope.resources.compiler.Diagnostic
import com.mussonindustrial.ignition.embr.periscope.resources.compiler.DiagnosticSeverity
import com.mussonindustrial.ignition.embr.periscope.resources.editor.components.CompilationStatusIcon.ShapeType
import com.mussonindustrial.ignition.embr.periscope.resources.editor.parser.DiagnosticParser
import com.mussonindustrial.ignition.embr.periscope.resources.language.Location
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dialog
import java.awt.Frame
import java.awt.Toolkit
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import javax.swing.AbstractAction
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.KeyStroke
import javax.swing.SwingUtilities
import javax.swing.border.EtchedBorder
import org.fife.rsta.ui.CollapsibleSectionPanel
import org.fife.rsta.ui.GoToDialog
import org.fife.rsta.ui.search.FindToolBar
import org.fife.rsta.ui.search.ReplaceToolBar
import org.fife.ui.rsyntaxtextarea.ErrorStrip
import org.fife.ui.rtextarea.Gutter
import org.fife.ui.rtextarea.RTextScrollPane

class ScriptEditor(text: String = "", syntaxStyle: String) : JPanel(BorderLayout()) {

    val textArea =
        ScriptTextArea(text).apply {
            syntaxEditingStyle = syntaxStyle
            antiAliasingEnabled = true
            isCodeFoldingEnabled = true
            tabSize = 2
            tabsEmulated = true
            paintTabLines = true
            insertPairedCharacters = true
        }

    val scrollPane = RTextScrollPane(textArea).apply { isIconRowHeaderEnabled = true }

    val gutter: Gutter
        get() = scrollPane.gutter

    var diagnostics: List<Diagnostic> = emptyList()
        private set

    private val parser = DiagnosticParser()

    init {
        textArea.addParser(parser)
        val editor =
            JPanel(BorderLayout()).apply {
                add(scrollPane, BorderLayout.CENTER)
                add(
                    ErrorStrip(textArea).apply {
                        border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)
                        followCaret = false
                    },
                    BorderLayout.EAST,
                )
            }
        add(createSearchPanel(editor), BorderLayout.CENTER)
    }

    private fun createSearchPanel(editor: JComponent): CollapsibleSectionPanel {
        val listener = TextSearchListener(textArea)
        val find = FindToolBar(listener)
        val replace = ReplaceToolBar(listener).apply { searchContext = find.searchContext }

        return CollapsibleSectionPanel().apply {
            add(editor)
            bindKey(FIND_KEY, addBottomComponent(FIND_KEY, find)::actionPerformed)
            bindKey(REPLACE_KEY, addBottomComponent(REPLACE_KEY, replace)::actionPerformed)
            bindKey(GO_TO_LINE_KEY) { promptGoToLine() }
        }
    }

    private fun promptGoToLine() {
        val dialog =
            when (val window = SwingUtilities.getWindowAncestor(this)) {
                is Frame -> GoToDialog(window)
                is Dialog -> GoToDialog(window)
                else -> return
            }
        dialog.maxLineNumberAllowed = textArea.lineCount
        dialog.isVisible = true
        if (dialog.lineNumber > 0) {
            textArea.caretPosition = textArea.getLineStartOffset(dialog.lineNumber - 1)
        }
    }

    fun showDiagnostics(diagnostics: List<Diagnostic>) {
        this.diagnostics = diagnostics
        parser.setDiagnostics(diagnostics)
        textArea.forceReparsing(parser)
        gutter.removeAllTrackingIcons()
        diagnostics.forEach { diagnostic ->
            val line = diagnostic.range?.start?.line ?: return@forEach
            if (line <= textArea.lineCount) {
                gutter.addLineTrackingIcon(line - 1, iconOf(diagnostic), diagnostic.message)
            }
        }
    }

    private fun iconOf(diagnostic: Diagnostic): CompilationStatusIcon {
        return when (diagnostic.severity) {
            DiagnosticSeverity.ERROR -> CompilationStatusIcon(ShapeType.ERROR, Color.RED)
            DiagnosticSeverity.WARNING -> CompilationStatusIcon(ShapeType.WARNING, Color.ORANGE)
            DiagnosticSeverity.INFO -> CompilationStatusIcon(ShapeType.DOT, Color.BLUE)
        }
    }

    fun bindKey(key: KeyStroke, action: (ActionEvent) -> Unit) {
        textArea.getInputMap(WHEN_FOCUSED).put(key, key.toString())
        textArea.actionMap.put(
            key.toString(),
            object : AbstractAction() {
                override fun actionPerformed(e: ActionEvent) {
                    action(e)
                }
            },
        )
    }

    fun select(start: Int, length: Int) {
        val end = (start + length).coerceAtMost(textArea.document.length)
        textArea.caretPosition = start.coerceIn(0, end)
        textArea.moveCaretPosition(end)
        textArea.requestFocusInWindow()
    }

    fun reveal(location: Location) {
        select(location.start, location.length)
    }

    companion object {
        val MENU_SHORTCUT = Toolkit.getDefaultToolkit().menuShortcutKeyMaskEx
        private val FIND_KEY: KeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F, MENU_SHORTCUT)
        private val REPLACE_KEY: KeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_R, MENU_SHORTCUT)
        private val GO_TO_LINE_KEY: KeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_G, MENU_SHORTCUT)

        fun menuItem(text: String, key: KeyStroke?, action: () -> Unit): JMenuItem {
            return JMenuItem(text).apply {
                accelerator = key
                addActionListener { action() }
            }
        }
    }
}
