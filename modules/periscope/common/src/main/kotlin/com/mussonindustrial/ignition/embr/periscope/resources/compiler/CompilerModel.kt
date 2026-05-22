package com.mussonindustrial.ignition.embr.periscope.resources.compiler

import com.inductiveautomation.ignition.common.gson.JsonDeserializationContext
import com.inductiveautomation.ignition.common.gson.JsonDeserializer
import com.inductiveautomation.ignition.common.gson.JsonElement
import com.inductiveautomation.ignition.common.gson.JsonPrimitive
import com.inductiveautomation.ignition.common.gson.JsonSerializationContext
import com.inductiveautomation.ignition.common.gson.JsonSerializer
import java.lang.reflect.Type

data class CompileRequest(val source: String, val path: String)

data class Position(val line: Int, val column: Int, val offset: Int)

data class Range(val start: Position, val end: Position)

enum class DiagnosticSeverity {
    ERROR,
    WARNING,
    INFO,
}

data class Diagnostic(
    val message: String,
    val severity: DiagnosticSeverity,
    val range: Range? = null,
    val code: String? = null,
    val source: String? = null,
    val stack: String? = null,
)

data class CompileResult(
    val success: Boolean,
    val output: String? = null,
    val diagnostics: List<Diagnostic> = emptyList(),
)

data class FormatRequest(val source: String, val path: String, val cursor: Int)

data class FormatResult(
    val success: Boolean,
    val output: String,
    val cursor: Int,
    val diagnostics: List<Diagnostic> = emptyList(),
)

class DiagnosticSeverityAdapter :
    JsonSerializer<DiagnosticSeverity>, JsonDeserializer<DiagnosticSeverity> {

    override fun serialize(
        src: DiagnosticSeverity,
        typeOfSrc: Type,
        context: JsonSerializationContext,
    ): JsonElement {
        return JsonPrimitive(
            when (src) {
                DiagnosticSeverity.ERROR -> "error"
                DiagnosticSeverity.WARNING -> "warning"
                DiagnosticSeverity.INFO -> "info"
            }
        )
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext,
    ): DiagnosticSeverity {
        return when (json.asString.lowercase()) {
            "error" -> DiagnosticSeverity.ERROR
            "warning" -> DiagnosticSeverity.WARNING
            "info" -> DiagnosticSeverity.INFO

            else -> error("Unknown DiagnosticSeverity: ${json.asString}")
        }
    }
}
