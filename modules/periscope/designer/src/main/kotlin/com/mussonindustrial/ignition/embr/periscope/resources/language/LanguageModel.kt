package com.mussonindustrial.ignition.embr.periscope.resources.language

import com.inductiveautomation.ignition.common.gson.JsonElement
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.mussonindustrial.ignition.embr.periscope.resources.compiler.Diagnostic

data class ResourceFile(val path: String, val kind: String, val source: String) {
    companion object {
        const val TYPESCRIPT = "typescript"
        const val JAVASCRIPT = "javascript"
        const val CSS = "css"
        const val LIBRARY = "library"
    }
}

data class SetFilesRequest(val files: List<ResourceFile>)

data class FileRequest(val path: String)

data class PositionRequest(val path: String, val offset: Int, val prefix: String? = null)

data class FormatOnTypeRequest(val path: String, val offset: Int, val key: String)

data class Span(val start: Int, val length: Int) {
    val end: Int
        get() = start + length

    operator fun contains(offset: Int) = offset in start..end
}

data class Location(
    val path: String?,
    val start: Int,
    val length: Int,
    val line: Int,
    val lineText: String,
)

data class TextEdit(val start: Int, val length: Int, val newText: String)

data class FileEdits(val path: String, val edits: List<TextEdit>)

data class CodeAction(val description: String, val changes: List<FileEdits>)

data class ClassificationsResult(val kinds: List<String>, val tokens: IntArray)

data class DiagnosticsResult(val diagnostics: List<Diagnostic>)

data class CompletionEntry(
    val name: String,
    val kind: String,
    val kindModifiers: String,
    val insertText: String?,
    val replacementSpan: Span?,
    val source: String?,
    val data: JsonElement?,
    val hasAction: Boolean,
)

data class CompletionsResult(val entries: List<CompletionEntry>)

data class CompletionDetailsRequest(
    val path: String,
    val offset: Int,
    val name: String,
    val source: String?,
    val data: JsonElement?,
)

data class CompletionDetailsResult(
    val display: String,
    val documentationHtml: String,
    val codeActions: List<CodeAction>,
)

data class QuickInfoResult(val span: Span, val display: String, val documentationHtml: String)

data class DefinitionResult(val span: Span, val definitions: List<Location>) {
    val target: Location?
        get() = definitions.firstOrNull { it.path != null }
}

data class ReferencesResult(val references: List<Location>)

data class HighlightsResult(val highlights: List<Span>)

data class SignatureItem(
    val prefix: String,
    val parameters: List<String>,
    val separator: String,
    val suffix: String,
    val documentationHtml: String,
)

data class SignatureHelpResult(
    val items: List<SignatureItem>,
    val selectedItem: Int,
    val argumentIndex: Int,
)

data class CodeFixesRequest(val path: String, val start: Int, val end: Int)

data class CodeFixesResult(val actions: List<CodeAction>)

data class EditsResult(val edits: List<TextEdit>)

data class JsxClosingTagResult(val newText: String)

data class LinkedEditingResult(val ranges: List<Span>)

data class OutlineItem(
    val text: String,
    val detail: String,
    val kind: String,
    val span: Span,
    val children: List<OutlineItem>,
)

data class OutlineResult(val items: List<OutlineItem>)

data class ComponentSize(val width: Int, val height: Int)

data class ComponentEventInfo(
    val name: String,
    val description: String? = null,
    val schema: JsonObject,
)

data class ComponentVariantInfo(
    val name: String,
    val description: String? = null,
    val defaultSize: ComponentSize? = null,
    val props: JsonObject,
)

data class ComponentInfo(
    val id: String,
    val name: String,
    val description: String? = null,
    val icon: String? = null,
    val library: String? = null,
    val category: String? = null,
    val defaultSize: ComponentSize? = null,
    val source: String? = null,
    val events: List<ComponentEventInfo>? = null,
    val diagnostics: List<String>? = null,
    val position: JsonObject? = null,
    val variants: List<ComponentVariantInfo>? = null,
    val defaults: JsonObject,
    val schema: JsonObject,
)

data class ComponentFailure(val path: String, val message: String)

data class ComponentsRequest(val revision: Int?)

data class ComponentLibraryInfo(
    val path: String,
    val namespace: String,
    val name: String? = null,
    val icon: String? = null,
) {
    val displayName: String
        get() = name ?: namespace
}

data class ComponentsResult(
    val revision: Int,
    val components: List<ComponentInfo>?,
    val libraries: List<ComponentLibraryInfo>?,
    val failures: List<ComponentFailure>?,
)

data class SymbolsRequest(val query: String)

data class SymbolItem(
    val name: String,
    val kind: String,
    val containerName: String,
    val location: Location,
)

data class SymbolsResult(val symbols: List<SymbolItem>)

data class InlayHint(val text: String, val position: Int, val kind: String) {
    companion object {
        const val PARAMETER = "parameter"
    }
}

data class InlayHintsResult(val hints: List<InlayHint>)
