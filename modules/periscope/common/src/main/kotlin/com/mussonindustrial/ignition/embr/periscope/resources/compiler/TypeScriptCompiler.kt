package com.mussonindustrial.ignition.embr.periscope.resources.compiler

import com.mussonindustrial.ignition.embr.periscope.js.DesignerWebBridge

class TypeScriptCompiler(bridge: DesignerWebBridge) : AbstractCompiler(bridge) {
    override val service: String = "typescript.compiler"
    override val version: String = "0.1.0"
}
