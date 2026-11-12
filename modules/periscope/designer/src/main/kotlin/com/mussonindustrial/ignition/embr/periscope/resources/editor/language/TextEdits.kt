package com.mussonindustrial.ignition.embr.periscope.resources.editor.language

import com.mussonindustrial.ignition.embr.periscope.resources.language.TextEdit
import org.fife.ui.rtextarea.RTextArea

fun RTextArea.applyEdits(edits: List<TextEdit>) {
    beginAtomicEdit()
    try {
        edits
            .sortedByDescending { it.start }
            .forEach { replaceRange(it.newText, it.start, it.start + it.length) }
    } finally {
        endAtomicEdit()
    }
}

fun List<TextEdit>.shift(offset: Int): Int {
    return offset +
        filter { it.start + it.length <= offset }.sumOf { it.newText.length - it.length }
}
