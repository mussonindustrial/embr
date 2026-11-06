package com.mussonindustrial.ignition.embr.periscope.resources.language

import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.mussonindustrial.ignition.embr.periscope.resources.compiler.Compiler
import com.mussonindustrial.ignition.embr.periscope.resources.compiler.CompilerBridge
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

class TypeScriptLanguageService(private val bridge: CompilerBridge) {

    private val logger = this.getLoggerEx()
    private val gson = Compiler.gson
    private val executor = Executors.newSingleThreadExecutor {
        Thread(it, "embr-periscope-language").apply { isDaemon = true }
    }

    fun <T> background(block: () -> T): CompletableFuture<T> =
        CompletableFuture.supplyAsync(block, executor)

    fun setTypings() {
        val typings = javaClass.getResource(TYPINGS_PATH)
        if (typings == null) {
            logger.error("Typings '$TYPINGS_PATH' not found.")
            return
        }
        invoke("setTypings", typings.readText())
    }

    fun warmUp() {
        call<Any>("warmUp", Any())
    }

    fun removeFile(path: String) {
        call<Any>("removeFile", FileRequest(path))
    }

    fun setFiles(files: List<ResourceFile>) {
        call<Any>("setFiles", SetFilesRequest(files))
    }

    fun updateFile(file: ResourceFile) {
        call<Any>("updateFile", file)
    }

    fun classifications(path: String): ClassificationsResult? =
        call("classifications", FileRequest(path))

    fun diagnostics(path: String): DiagnosticsResult? = call("diagnostics", FileRequest(path))

    fun completions(path: String, offset: Int, prefix: String): CompletionsResult? =
        call("completions", PositionRequest(path, offset, prefix))

    fun completionDetails(path: String, offset: Int, entry: CompletionEntry) =
        call<CompletionDetailsResult>(
            "completionDetails",
            CompletionDetailsRequest(path, offset, entry.name, entry.source, entry.data),
        )

    fun quickInfo(path: String, offset: Int): QuickInfoResult? =
        call("quickInfo", PositionRequest(path, offset))

    fun definition(path: String, offset: Int): DefinitionResult? =
        call("definition", PositionRequest(path, offset))

    fun references(path: String, offset: Int): ReferencesResult? =
        call("references", PositionRequest(path, offset))

    fun highlights(path: String, offset: Int): HighlightsResult? =
        call("highlights", PositionRequest(path, offset))

    fun signatureHelp(path: String, offset: Int): SignatureHelpResult? =
        call("signatureHelp", PositionRequest(path, offset))

    fun codeFixes(path: String, start: Int, end: Int): CodeFixesResult? =
        call("codeFixes", CodeFixesRequest(path, start, end))

    fun organizeImports(path: String): EditsResult? = call("organizeImports", FileRequest(path))

    fun formatOnType(path: String, offset: Int, key: Char): EditsResult? =
        call("formatOnType", FormatOnTypeRequest(path, offset, key.toString()))

    fun linkedEditing(path: String, offset: Int): LinkedEditingResult? =
        call("linkedEditing", PositionRequest(path, offset))

    fun jsxClosingTag(path: String, offset: Int): JsxClosingTagResult? =
        call("jsxClosingTag", PositionRequest(path, offset))

    fun outline(path: String): OutlineResult? = call("outline", FileRequest(path))

    fun symbols(query: String): SymbolsResult? = call("symbols", SymbolsRequest(query))

    fun inlayHints(path: String): InlayHintsResult? = call("inlayHints", FileRequest(path))

    private inline fun <reified T> call(method: String, request: Any): T? {
        val result = invoke(method, gson.toJson(request)) ?: return null
        return gson.fromJson(result, T::class.java)
    }

    private fun invoke(method: String, payload: String): String? {
        return try {
            bridge.invoke(compiler = "language", method = method, payload)
        } catch (e: Exception) {
            logger.warn("Language service call '$method' failed.", e)
            null
        }
    }

    companion object {
        private const val TYPINGS_PATH = "/static/embr-periscope-designer-web-typings.json"
    }
}
