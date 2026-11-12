package com.mussonindustrial.ignition.embr.periscope.resources.compiler

import com.mussonindustrial.ignition.embr.periscope.js.DesignerWebBridge
import java.util.concurrent.CompletableFuture

abstract class AbstractCompiler(private val bridge: DesignerWebBridge) : Compiler {

    abstract val service: String
    val gson = Compiler.gson

    override fun compile(request: CompileRequest): CompletableFuture<CompileResult> =
        bridge.invoke(service, "compile", gson.toJson(request)) {
            gson.fromJson(it, CompileResult::class.java)
        }

    override fun format(request: FormatRequest): CompletableFuture<FormatResult> =
        bridge.invoke(service, "format", gson.toJson(request)) {
            gson.fromJson(it, FormatResult::class.java)
        }
}
