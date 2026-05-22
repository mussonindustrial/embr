package com.mussonindustrial.ignition.embr.periscope.resources.compiler

class TypeScriptCompiler(bridge: CompilerBridge) : AbstractCompiler(bridge) {
    override val compilerKey: String = "typescript"
    override val version: String = "0.1.0"
}
