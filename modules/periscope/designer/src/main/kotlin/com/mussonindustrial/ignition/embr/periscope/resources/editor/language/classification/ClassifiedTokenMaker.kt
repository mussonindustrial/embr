package com.mussonindustrial.ignition.embr.periscope.resources.editor.language.classification

import javax.swing.text.Segment
import org.fife.ui.rsyntaxtextarea.Token
import org.fife.ui.rsyntaxtextarea.modes.TypeScriptTokenMaker

class ClassifiedTokenMaker : TypeScriptTokenMaker() {

    var classifications: Classifications? = null

    override fun getTokenList(text: Segment, initialTokenType: Int, startOffset: Int): Token {
        val classifications = classifications
        if (classifications == null || text.count == 0) {
            return super.getTokenList(text, initialTokenType, startOffset)
        }

        resetTokenList()
        val lineEnd = startOffset + text.count
        var position = startOffset
        classifications.forEachIn(startOffset, lineEnd) { start, end, tokenType ->
            addGap(text, startOffset, position, start)
            addSegment(text, startOffset, start, end, tokenType)
            position = end
        }
        addGap(text, startOffset, position, lineEnd)
        addNullToken()
        return firstToken
    }

    override fun getLastTokenTypeOnLine(text: Segment, initialTokenType: Int): Int {
        if (classifications == null) return super.getLastTokenTypeOnLine(text, initialTokenType)
        return Token.NULL
    }

    private fun addGap(text: Segment, lineStart: Int, start: Int, end: Int) {
        if (end <= start) return
        val blank =
            (start until end).all { text.array[text.offset + it - lineStart].isWhitespace() }
        addSegment(text, lineStart, start, end, if (blank) Token.WHITESPACE else Token.IDENTIFIER)
    }

    private fun addSegment(text: Segment, lineStart: Int, start: Int, end: Int, type: Int) {
        val index = text.offset + start - lineStart
        addToken(text.array, index, index + end - start - 1, type, start)
    }
}
