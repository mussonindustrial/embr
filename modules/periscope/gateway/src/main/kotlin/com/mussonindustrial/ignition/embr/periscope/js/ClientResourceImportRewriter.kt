package com.mussonindustrial.ignition.embr.periscope.js

import com.mussonindustrial.ignition.embr.periscope.PeriscopeRoutes
import com.mussonindustrial.ignition.embr.periscope.js.ImportRewriteRuleSetBuilder.Companion.registry
import com.mussonindustrial.ignition.embr.periscope.js.modules.GlobalModule

class ClientResourceImportRewriter(
    val projectName: String,
    val hash: String,
    val resourcePath: String,
) : JavaScriptImportRewriter {

    companion object {
        const val PROJECT_PREFIX = "@/"
        const val SYSTEM_PREFIX = "system:"
        const val WEBDEV_PREFIX = "@webdev/"
        private val RELATIVE_PREFIXES = listOf("./", "../")

        fun isRelative(specifier: String): Boolean {
            return RELATIVE_PREFIXES.any { specifier.startsWith(it) }
        }

        fun resolveRelative(path: String, specifier: String): String? {
            val segments = path.split('/').dropLast(1).toMutableList()

            specifier.split('/').forEach { segment ->
                when (segment) {
                    "",
                    "." -> {}
                    ".." -> {
                        if (segments.isEmpty()) {
                            return null
                        }
                        segments.removeAt(segments.lastIndex)
                    }
                    else -> segments += segment
                }
            }

            val resolved = segments.joinToString("/")
            if (resolved.isEmpty()) {
                return null
            }
            return resolved
        }
    }

    override val rules = registry {
        preserve("http://")
        preserve("https://")
        preserve("/")
        matching(::isRelative) { specifier ->
            resolveRelative(resourcePath, specifier)?.let {
                PeriscopeRoutes.clientResource(projectName, hash, it)
            }
        }
        exact(GlobalModule.all.map { it.name }) { PeriscopeRoutes.globalModule(hash, it) }
        stripPrefix(SYSTEM_PREFIX) { PeriscopeRoutes.systemModule(hash, it) }
        stripPrefix(PROJECT_PREFIX) { PeriscopeRoutes.clientResource(projectName, hash, it) }
        stripPrefix(WEBDEV_PREFIX) { "/main/system/webdev/${projectName}/$it" }
    }
}
