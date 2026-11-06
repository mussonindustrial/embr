package com.mussonindustrial.ignition.embr.periscope.resources.editor.language.classification

import org.fife.ui.rsyntaxtextarea.Token

// Must stay in the order the bridge numbers them.
enum class TokenKind(val tokenType: Int) {
    TEXT(Token.IDENTIFIER),
    KEYWORD(Token.RESERVED_WORD),
    LINE_COMMENT(Token.COMMENT_EOL),
    BLOCK_COMMENT(Token.COMMENT_MULTILINE),
    DOC_COMMENT(Token.COMMENT_DOCUMENTATION),
    DOC_COMMENT_TAG(Token.COMMENT_KEYWORD),
    STRING(Token.LITERAL_STRING_DOUBLE_QUOTE),
    NUMBER(Token.LITERAL_NUMBER_DECIMAL_INT),
    REGEX(Token.REGEX),
    OPERATOR(Token.OPERATOR),
    BRACKET(Token.SEPARATOR),
    PUNCTUATION(Token.IDENTIFIER),
    TYPE(Token.DATA_TYPE),
    FUNCTION(Token.FUNCTION),
    PARAMETER(Token.VARIABLE),
    VARIABLE(Token.IDENTIFIER),
    CONSTANT(Token.IDENTIFIER),
    PROPERTY(Token.IDENTIFIER),
    JSX_TAG(Token.MARKUP_TAG_NAME),
    JSX_ATTRIBUTE(Token.MARKUP_TAG_ATTRIBUTE),
    JSX_TEXT(Token.IDENTIFIER);

    companion object {
        private val kinds = entries.toTypedArray()

        fun tokenTypeOf(kind: Int): Int = kinds.getOrNull(kind)?.tokenType ?: Token.IDENTIFIER
    }
}
