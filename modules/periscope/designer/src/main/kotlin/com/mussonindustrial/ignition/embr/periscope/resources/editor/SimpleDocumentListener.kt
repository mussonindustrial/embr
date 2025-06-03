package com.mussonindustrial.ignition.embr.periscope.resources.editor

import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class SimpleDocumentListener(val block: (DocumentEvent) -> Unit) : DocumentListener {
    override fun insertUpdate(event: DocumentEvent) {
        block(event)
    }

    override fun removeUpdate(event: DocumentEvent) {
        block(event)
    }

    override fun changedUpdate(event: DocumentEvent) {
        block(event)
    }
}
