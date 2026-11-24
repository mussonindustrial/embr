package com.mussonindustrial.ignition.embr.periscope.js

class JavaScriptImportScanner(private val source: String) {

    companion object {
        private const val CLAUSE_PUNCTUATION = "{},*"
        private val CONDITION_KEYWORDS = setOf("if", "while", "for", "with")
        private val REGEX_KEYWORDS =
            setOf(
                "return",
                "typeof",
                "instanceof",
                "in",
                "of",
                "new",
                "delete",
                "void",
                "throw",
                "case",
                "do",
                "else",
                "yield",
                "await",
            )
    }

    private val tokens = mutableListOf<Token>()
    private val templateBraces = ArrayDeque<Boolean>()
    private val conditionParentheses = ArrayDeque<Boolean>()
    private var position = 0

    fun scan(): List<JavaScriptImport> {
        tokenize()
        return tokens.indices
            .mapNotNull { index ->
                when {
                    isKeyword(index, "import") -> importAt(index)
                    isKeyword(index, "export") -> exportAt(index)
                    else -> null
                }
            }
            .distinctBy { it.range.first }
    }

    private fun tokenize() {
        while (position < source.length) {
            val char = source[position]
            when {
                char.isWhitespace() -> position++
                char == '/' && peekNext() == '/' -> skipLineComment()
                char == '/' && peekNext() == '*' -> skipBlockComment()
                char == '/' && !followsValue() -> readRegex()
                char == '\'' || char == '"' -> readString(char)
                char == '`' -> {
                    position++
                    readTemplate()
                }
                char.isJavaIdentifierStart() -> readIdentifier()
                char.isDigit() -> readNumber()
                else -> readPunctuation(char)
            }
        }
    }

    private fun peekNext(): Char? {
        return source.getOrNull(position + 1)
    }

    private fun addToken(kind: TokenKind, start: Int, endsCondition: Boolean = false) {
        tokens += Token(kind, start, position, endsCondition)
    }

    private fun skipLineComment() {
        val end = source.indexOf('\n', position)
        position = if (end < 0) source.length else end
    }

    private fun skipBlockComment() {
        val end = source.indexOf("*/", position + 2)
        position = if (end < 0) source.length else end + 2
    }

    private fun readString(quote: Char) {
        val start = position
        position++

        while (position < source.length) {
            val char = source[position]
            if (char == '\\') {
                position += 2
            } else if (char == quote) {
                position++
                break
            } else if (char == '\n') {
                break
            } else {
                position++
            }
        }

        position = position.coerceAtMost(source.length)
        addToken(TokenKind.STRING, start)
    }

    private fun readTemplate() {
        val start = position

        while (position < source.length) {
            val char = source[position]
            if (char == '\\') {
                position += 2
            } else if (char == '`') {
                position++
                addToken(TokenKind.LITERAL, start)
                return
            } else if (char == '$' && peekNext() == '{') {
                position += 2
                templateBraces.addLast(true)
                return
            } else {
                position++
            }
        }

        position = source.length
    }

    private fun readRegex() {
        val start = position
        var inClass = false
        position++

        while (position < source.length) {
            val char = source[position]
            if (char == '\\') {
                position += 2
            } else if (char == '\n' || (char == '/' && !inClass)) {
                position++
                break
            } else {
                when (char) {
                    '[' -> inClass = true
                    ']' -> inClass = false
                }
                position++
            }
        }

        position = position.coerceAtMost(source.length)
        skipIdentifierPart()
        addToken(TokenKind.LITERAL, start)
    }

    private fun readIdentifier() {
        val start = position
        skipIdentifierPart()
        addToken(TokenKind.IDENTIFIER, start)
    }

    private fun readNumber() {
        val start = position
        while (
            position < source.length &&
                (source[position].isLetterOrDigit() || source[position] in "._")
        ) {
            position++
        }
        addToken(TokenKind.LITERAL, start)
    }

    private fun skipIdentifierPart() {
        while (position < source.length && source[position].isJavaIdentifierPart()) {
            position++
        }
    }

    private fun readPunctuation(char: Char) {
        val start = position
        position++

        when (char) {
            '{' -> templateBraces.addLast(false)
            '}' -> {
                if (templateBraces.removeLastOrNull() == true) {
                    readTemplate()
                    return
                }
            }
            '(' -> conditionParentheses.addLast(isConditionKeyword(tokens.lastIndex))
            ')' -> {
                addToken(
                    TokenKind.PUNCTUATION,
                    start,
                    conditionParentheses.removeLastOrNull() == true,
                )
                return
            }
        }

        addToken(TokenKind.PUNCTUATION, start)
    }

    private fun isConditionKeyword(index: Int): Boolean {
        val token = tokens.getOrNull(index) ?: return false
        return token.kind == TokenKind.IDENTIFIER && textOf(token) in CONDITION_KEYWORDS
    }

    private fun followsValue(): Boolean {
        val last = tokens.lastOrNull() ?: return false

        return when (last.kind) {
            TokenKind.IDENTIFIER -> textOf(last) !in REGEX_KEYWORDS
            TokenKind.STRING,
            TokenKind.LITERAL -> true
            TokenKind.PUNCTUATION ->
                when (source[last.start]) {
                    ')' -> !last.endsCondition
                    ']',
                    '}' -> true
                    '+',
                    '-' -> isIncrement(last)
                    else -> false
                }
        }
    }

    private fun isIncrement(token: Token): Boolean {
        val previous = tokens.getOrNull(tokens.lastIndex - 1) ?: return false
        return previous.end == token.start && source[previous.start] == source[token.start]
    }

    private fun textOf(token: Token): String {
        return source.substring(token.start, token.end)
    }

    private fun isIdentifier(index: Int, text: String): Boolean {
        val token = tokens.getOrNull(index) ?: return false
        return token.kind == TokenKind.IDENTIFIER &&
            token.end - token.start == text.length &&
            source.startsWith(text, token.start)
    }

    private fun isPunctuation(index: Int, char: Char): Boolean {
        val token = tokens.getOrNull(index) ?: return false
        return token.kind == TokenKind.PUNCTUATION && source[token.start] == char
    }

    private fun isString(index: Int): Boolean {
        return tokens.getOrNull(index)?.kind == TokenKind.STRING
    }

    private fun isKeyword(index: Int, text: String): Boolean {
        return isIdentifier(index, text) && !isPunctuation(index - 1, '.')
    }

    private fun importAt(index: Int): JavaScriptImport? {
        if (isPunctuation(index + 1, '(')) {
            val isLiteral = isPunctuation(index + 3, ')') || isPunctuation(index + 3, ',')
            return if (isLiteral) specifierAt(index + 2) else null
        }

        return specifierAt(index + 1) ?: findFromClause(index + 1)
    }

    private fun exportAt(index: Int): JavaScriptImport? {
        if (!isPunctuation(index + 1, '*') && !isPunctuation(index + 1, '{')) {
            return null
        }

        return findFromClause(index + 1)
    }

    private fun findFromClause(start: Int): JavaScriptImport? {
        var index = start

        while (isClauseToken(index)) {
            if (isIdentifier(index, "from") && isString(index + 1)) {
                return specifierAt(index + 1)
            }
            if (isPunctuation(index, '}') && !isIdentifier(index + 1, "from")) {
                return null
            }
            index++
        }

        return null
    }

    private fun isClauseToken(index: Int): Boolean {
        val token = tokens.getOrNull(index) ?: return false

        return when (token.kind) {
            TokenKind.IDENTIFIER,
            TokenKind.STRING -> true
            TokenKind.PUNCTUATION -> source[token.start] in CLAUSE_PUNCTUATION
            TokenKind.LITERAL -> false
        }
    }

    private fun specifierAt(index: Int): JavaScriptImport? {
        if (!isString(index)) {
            return null
        }

        val token = tokens[index]
        val range = (token.start + 1) until (token.end - 1).coerceAtLeast(token.start + 1)
        return JavaScriptImport(source.substring(range.first, range.last + 1), range)
    }

    private enum class TokenKind {
        IDENTIFIER,
        STRING,
        PUNCTUATION,
        LITERAL,
    }

    private class Token(
        val kind: TokenKind,
        val start: Int,
        val end: Int,
        val endsCondition: Boolean = false,
    )
}
