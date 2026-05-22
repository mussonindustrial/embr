package com.mussonindustrial.ignition.embr.periscope.resources.compiler

import com.inductiveautomation.ignition.common.gson.Gson
import com.inductiveautomation.ignition.common.gson.GsonBuilder

interface Compiler {

    companion object {
        val gson: Gson =
            GsonBuilder()
                .registerTypeAdapter(DiagnosticSeverity::class.java, DiagnosticSeverityAdapter())
                .serializeNulls()
                .setPrettyPrinting()
                .create()
    }

    val version: String

    fun compile(request: CompileRequest): CompileResult

    fun format(request: FormatRequest): FormatResult
}
