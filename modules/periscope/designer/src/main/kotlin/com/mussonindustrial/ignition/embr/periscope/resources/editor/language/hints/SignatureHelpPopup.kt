package com.mussonindustrial.ignition.embr.periscope.resources.editor.language.hints

import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.Html
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.LanguageSession
import com.mussonindustrial.ignition.embr.periscope.resources.language.SignatureHelpResult
import java.awt.Point
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JWindow
import javax.swing.SwingUtilities
import javax.swing.Timer
import javax.swing.UIManager
import javax.swing.event.CaretListener

class SignatureHelpPopup(private val session: LanguageSession) {

    private val textArea = session.textArea
    private var window: JWindow? = null
    private val label =
        JLabel().apply {
            isOpaque = true
            background = UIManager.getColor("ToolTip.background")
            foreground = UIManager.getColor("ToolTip.foreground")
            border =
                BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UIManager.getColor("ToolTip.foreground")),
                    BorderFactory.createEmptyBorder(4, 6, 4, 6),
                )
        }
    private val debounce = Timer(100) { update() }.apply { isRepeats = false }

    private val keyListener =
        object : KeyAdapter() {
            override fun keyTyped(e: KeyEvent) {
                if (e.keyChar == '(' || e.keyChar == ',') debounce.restart()
            }

            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_ESCAPE) hide()
            }
        }

    private val caretListener = CaretListener { if (window?.isVisible == true) debounce.restart() }

    private val focusListener =
        object : FocusAdapter() {
            override fun focusLost(e: FocusEvent) = hide()
        }

    fun install() {
        textArea.addKeyListener(keyListener)
        textArea.addCaretListener(caretListener)
        textArea.addFocusListener(focusListener)
    }

    fun uninstall() {
        debounce.stop()
        textArea.removeKeyListener(keyListener)
        textArea.removeCaretListener(caretListener)
        textArea.removeFocusListener(focusListener)
        window?.dispose()
        window = null
    }

    fun show() {
        debounce.restart()
    }

    private fun update() {
        session.sync()
        val help = session.service.signatureHelp(session.path, textArea.caretPosition)
        if (help == null || help.items.isEmpty()) {
            hide()
            return
        }
        label.text = render(help)
        showAtCaret()
    }

    private fun showAtCaret() {
        val owner = SwingUtilities.getWindowAncestor(textArea) ?: return
        val popup = window ?: JWindow(owner).apply { contentPane.add(label) }.also { window = it }
        val caret = textArea.modelToView2D(textArea.caretPosition) ?: return
        val location = Point(caret.x.toInt(), caret.y.toInt())
        SwingUtilities.convertPointToScreen(location, textArea)

        popup.pack()
        popup.setLocation(location.x, location.y - popup.height - 2)
        popup.isVisible = true
    }

    private fun hide() {
        window?.isVisible = false
    }

    private fun render(help: SignatureHelpResult): String {
        val item = help.items[help.selectedItem.coerceIn(help.items.indices)]
        val parameters =
            item.parameters.mapIndexed { index, parameter ->
                val text = Html.escape(parameter)
                if (index == help.argumentIndex) "<b>$text</b>" else text
            }
        val signature =
            Html.escape(item.prefix) +
                parameters.joinToString(Html.escape(item.separator)) +
                Html.escape(item.suffix)
        return "<html><body style='width: 300px'><code>$signature</code>${item.documentationHtml}</body></html>"
    }
}
