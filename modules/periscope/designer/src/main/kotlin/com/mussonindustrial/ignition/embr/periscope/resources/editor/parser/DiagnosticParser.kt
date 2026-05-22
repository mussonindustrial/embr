package com.mussonindustrial.ignition.embr.periscope.resources.editor.parser

import com.mussonindustrial.ignition.embr.periscope.resources.compiler.Diagnostic
import com.mussonindustrial.ignition.embr.periscope.resources.compiler.DiagnosticSeverity
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument
import org.fife.ui.rsyntaxtextarea.parser.AbstractParser
import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult
import org.fife.ui.rsyntaxtextarea.parser.DefaultParserNotice
import org.fife.ui.rsyntaxtextarea.parser.ParseResult
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice

class DiagnosticParser : AbstractParser() {

    private var diagnostics: List<Diagnostic> = emptyList()

    fun setDiagnostics(diagnostics: List<Diagnostic>) {
        this.diagnostics = diagnostics
    }

    override fun parse(document: RSyntaxDocument, style: String?): ParseResult {
        val result = DefaultParseResult(this)

        diagnostics.forEach { d ->
            val range = d.range ?: return@forEach
            val start = range.start
            val end = range.end
            val offset = start.offset

            val length =
                if (end.offset > start.offset) {
                    end.offset - start.offset
                } else {
                    -1
                }

            val notice = DefaultParserNotice(this, d.message, start.line - 1, offset, length)

            notice.level =
                when (d.severity) {
                    DiagnosticSeverity.ERROR -> ParserNotice.Level.ERROR
                    DiagnosticSeverity.WARNING -> ParserNotice.Level.WARNING
                    DiagnosticSeverity.INFO -> ParserNotice.Level.INFO
                }

            result.addNotice(notice)
        }

        return result
    }
}
