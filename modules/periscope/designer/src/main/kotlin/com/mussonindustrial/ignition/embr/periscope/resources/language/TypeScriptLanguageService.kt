package com.mussonindustrial.ignition.embr.periscope.resources.language

import com.mussonindustrial.ignition.embr.periscope.js.DesignerWebBridge
import com.mussonindustrial.ignition.embr.periscope.resources.compiler.Compiler
import java.util.concurrent.CompletableFuture

class TypeScriptLanguageService(private val bridge: DesignerWebBridge) {

    private val gson = Compiler.gson

    fun setTypings(): CompletableFuture<Unit> {
        val typings =
            requireNotNull(javaClass.getResource(TYPINGS_PATH)) {
                "Typings '$TYPINGS_PATH' not found."
            }
        return bridge.invoke(SERVICE, "setTypings", typings.readText()) {}
    }

    fun warmUp() = call<Any>("warmUp", Any())

    fun removeFile(path: String) = call<Any>("removeFile", FileRequest(path))

    fun setFiles(files: List<ResourceFile>) = call<Any>("setFiles", SetFilesRequest(files))

    fun updateFile(file: ResourceFile) = call<Any>("updateFile", file)

    fun classifications(path: String) =
        call<ClassificationsResult>("classifications", FileRequest(path))

    fun diagnostics(path: String) = call<DiagnosticsResult>("diagnostics", FileRequest(path))

    fun completions(path: String, offset: Int, prefix: String) =
        call<CompletionsResult>("completions", PositionRequest(path, offset, prefix))

    fun completionDetails(path: String, offset: Int, entry: CompletionEntry) =
        call<CompletionDetailsResult>(
            "completionDetails",
            CompletionDetailsRequest(path, offset, entry.name, entry.source, entry.data),
        )

    fun quickInfo(path: String, offset: Int) =
        call<QuickInfoResult>("quickInfo", PositionRequest(path, offset))

    fun definition(path: String, offset: Int) =
        call<DefinitionResult>("definition", PositionRequest(path, offset))

    fun references(path: String, offset: Int) =
        call<ReferencesResult>("references", PositionRequest(path, offset))

    fun highlights(path: String, offset: Int) =
        call<HighlightsResult>("highlights", PositionRequest(path, offset))

    fun signatureHelp(path: String, offset: Int) =
        call<SignatureHelpResult>("signatureHelp", PositionRequest(path, offset))

    fun codeFixes(path: String, start: Int, end: Int) =
        call<CodeFixesResult>("codeFixes", CodeFixesRequest(path, start, end))

    fun organizeImports(path: String) = call<EditsResult>("organizeImports", FileRequest(path))

    fun formatOnType(path: String, offset: Int, key: Char) =
        call<EditsResult>("formatOnType", FormatOnTypeRequest(path, offset, key.toString()))

    fun linkedEditing(path: String, offset: Int) =
        call<LinkedEditingResult>("linkedEditing", PositionRequest(path, offset))

    fun jsxClosingTag(path: String, offset: Int) =
        call<JsxClosingTagResult>("jsxClosingTag", PositionRequest(path, offset))

    fun outline(path: String) = call<OutlineResult>("outline", FileRequest(path))

    fun reactComponentNames(path: String) =
        call<ExportsResult>("reactComponentNames", FileRequest(path))

    fun symbols(query: String) = call<SymbolsResult>("symbols", SymbolsRequest(query))

    fun inlayHints(path: String) = call<InlayHintsResult>("inlayHints", FileRequest(path))

    private inline fun <reified T> call(method: String, request: Any): CompletableFuture<T?> =
        bridge.invoke(SERVICE, method, gson.toJson(request)) { gson.fromJson(it, T::class.java) }

    companion object {
        private const val SERVICE = "typescript.language"
        private const val TYPINGS_PATH = "/static/embr-periscope-designer-web-typings.json"
    }
}
