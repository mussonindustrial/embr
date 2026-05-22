package com.mussonindustrial.ignition.embr.periscope.resources.compiler

abstract class AbstractCompiler(private val bridge: CompilerBridge) : Compiler {

    abstract val compilerKey: String
    val gson = Compiler.gson

    override fun compile(request: CompileRequest): CompileResult {
        val payload = gson.toJson(request, CompileRequest::class.java)
        val result = bridge.invoke(compiler = compilerKey, method = "compile", payload)
        return gson.fromJson(result, CompileResult::class.java)
    }

    override fun format(request: FormatRequest): FormatResult {
        val payload = gson.toJson(request, FormatRequest::class.java)
        val result = bridge.invoke(compiler = compilerKey, method = "format", payload)
        return gson.fromJson(result, FormatResult::class.java)
    }
}
