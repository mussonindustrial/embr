package com.mussonindustrial.ignition.embr.periscope.resources.editor.language.editing

import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.LanguageSession
import com.mussonindustrial.ignition.embr.periscope.resources.language.Span
import javax.swing.SwingUtilities
import javax.swing.Timer
import javax.swing.event.CaretListener
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.Document
import javax.swing.text.Position

class LinkedTagEditor(private val session: LanguageSession) {

    private val textArea = session.textArea
    private var tagNames: List<TagName> = emptyList()
    private var mirroring = false
    private val debounce = Timer(100) { find() }.apply { isRepeats = false }
    private val caretListener = CaretListener { debounce.restart() }

    private val documentListener =
        object : DocumentListener {
            override fun insertUpdate(event: DocumentEvent) = edited(event.offset)

            override fun removeUpdate(event: DocumentEvent) = edited(event.offset)

            override fun changedUpdate(event: DocumentEvent) {}
        }

    fun install() {
        textArea.addCaretListener(caretListener)
        textArea.document.addDocumentListener(documentListener)
    }

    fun uninstall() {
        debounce.stop()
        textArea.removeCaretListener(caretListener)
        textArea.document.removeDocumentListener(documentListener)
        tagNames = emptyList()
    }

    private fun find() {
        val caret = textArea.caretPosition
        session.request({ linkedEditing(session.path, caret) }) { result ->
            tagNames = result.ranges.map { TagName(textArea.document, it) }
        }
    }

    private fun edited(offset: Int) {
        if (mirroring) return
        val edited = tagNames.firstOrNull { offset in it } ?: return
        SwingUtilities.invokeLater { mirror(edited) }
    }

    private fun mirror(edited: TagName) {
        val name = edited.text()
        if (!name.matches(TAG_NAME)) {
            tagNames = emptyList()
            return
        }
        mirroring = true
        try {
            tagNames
                .filter { it !== edited && it.text() != name }
                .forEach { textArea.replaceRange(name, it.start, it.end) }
        } finally {
            mirroring = false
        }
    }

    private inner class TagName(document: Document, span: Span) {
        private val before: Position = document.createPosition(span.start - 1)
        private val after: Position = document.createPosition(span.end)

        val start: Int
            get() = before.offset + 1

        val end: Int
            get() = after.offset

        operator fun contains(offset: Int) = offset in start..end

        fun text(): String = textArea.getText(start, end - start)
    }

    companion object {
        private val TAG_NAME = Regex("[\\w$.:-]*")
    }
}
