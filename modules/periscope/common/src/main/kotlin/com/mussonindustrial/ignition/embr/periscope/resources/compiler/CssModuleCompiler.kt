package com.mussonindustrial.ignition.embr.periscope.resources.compiler

import com.mussonindustrial.ignition.embr.periscope.js.DesignerWebBridge

class CssModuleCompiler(bridge: DesignerWebBridge) : AbstractCompiler(bridge) {
    override val service: String = "css.compiler"
    override val version: String = "0.1.0"
}
