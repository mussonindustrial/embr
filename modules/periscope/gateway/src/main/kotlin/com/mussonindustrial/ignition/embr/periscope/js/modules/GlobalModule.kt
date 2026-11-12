package com.mussonindustrial.ignition.embr.periscope.js.modules

import com.inductiveautomation.ignition.common.gson.JsonParser

class GlobalModule(
    override val name: String,
    val global: String,
    override val namedExports: List<String>,
) : VirtualModule {

    override fun emit(): String {
        return buildString {
            appendLine("const m = globalThis['$global'];")
            appendLine("if (m == null) throw new Error(\"Perspective hasn't loaded '$name'.\");")
            appendLine("export default m;")
            namedExports.forEach { appendLine("export const $it = m.$it;") }
        }
    }

    companion object {
        val all: List<GlobalModule> by lazy {
            val perspectiveGlobals =
                GlobalModule::class.java.getResourceAsStream("perspective-globals.json")
                    ?: error("perspective-globals.json is missing")

            perspectiveGlobals
                .reader()
                .use { JsonParser.parseReader(it) }
                .asJsonObject
                .getAsJsonArray("globalModules")
                .map { it.asJsonObject }
                .map { module ->
                    GlobalModule(
                        module["specifier"].asString,
                        module["global"].asString,
                        module.getAsJsonArray("exports").map { it.asString },
                    )
                }
        }
    }
}
