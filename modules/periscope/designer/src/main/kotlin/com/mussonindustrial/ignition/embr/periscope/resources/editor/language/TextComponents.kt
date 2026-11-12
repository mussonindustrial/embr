package com.mussonindustrial.ignition.embr.periscope.resources.editor.language

import javax.swing.text.JTextComponent

fun lineBeforeCaret(comp: JTextComponent): String {
    val caret = comp.caretPosition
    val line = comp.document.defaultRootElement.let { it.getElement(it.getElementIndex(caret)) }
    return comp.document.getText(line.startOffset, caret - line.startOffset)
}

fun closesString(comp: JTextComponent): Boolean {
    val before = lineBeforeCaret(comp)
    val quote = before.lastOrNull()
    if (quote != '\'' && quote != '"') return false
    return before.count { it == quote } % 2 == 0
}
