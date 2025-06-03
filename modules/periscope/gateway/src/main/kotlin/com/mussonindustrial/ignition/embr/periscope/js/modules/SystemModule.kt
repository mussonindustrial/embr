package com.mussonindustrial.ignition.embr.periscope.js.modules

class SystemModule(override val name: String, override val namedExports: List<String>) :
    VirtualModule {

    override fun emit(): String {
        return buildString {
            appendLine("const m = Embr.clientResources.getSystemModule('${name}');")
            appendLine("export default m;")
            namedExports.forEach { appendLine("export const $it = m.$it;") }
        }
    }
}
