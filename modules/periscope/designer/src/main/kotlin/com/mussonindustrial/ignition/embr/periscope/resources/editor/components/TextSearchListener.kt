package com.mussonindustrial.ignition.embr.periscope.resources.editor.components

import org.fife.rsta.ui.search.SearchEvent
import org.fife.rsta.ui.search.SearchListener
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rtextarea.SearchEngine

class TextSearchListener(private val textArea: RSyntaxTextArea) : SearchListener {

    override fun searchEvent(e: SearchEvent) {
        val context = e.searchContext
        when (e.type) {
            SearchEvent.Type.MARK_ALL -> SearchEngine.markAll(textArea, context)
            SearchEvent.Type.FIND -> SearchEngine.find(textArea, context)
            SearchEvent.Type.REPLACE -> SearchEngine.replace(textArea, context)
            SearchEvent.Type.REPLACE_ALL -> SearchEngine.replaceAll(textArea, context)
            null -> {}
        }
    }

    override fun getSelectedText(): String? = textArea.selectedText
}
