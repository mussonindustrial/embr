package com.mussonindustrial.ignition.embr.periscope.resources.compiler

interface CompilerBridge {
    fun invoke(compiler: String, method: String, payload: String): String
}
