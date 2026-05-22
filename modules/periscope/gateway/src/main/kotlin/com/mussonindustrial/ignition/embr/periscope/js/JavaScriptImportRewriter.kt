package com.mussonindustrial.ignition.embr.periscope.js

interface JavaScriptImportRewriter {

    val rules: List<ImportRewriteRule>

    companion object {
        private val importExportRegex =
            """(\b(?:import|export)\b(?:\s+[\w\s{},*$]*?\s+from)?\s*)(['"])([^'"]+)\2""".toRegex()
    }

    fun rewrite(source: String): String {
        return importExportRegex.replace(source) { match ->
            val prefix = match.groupValues[1]
            val quote = match.groupValues[2]
            val spec = match.groupValues[3]

            val rewritten = rewriteSpecifier(spec)
            Regex.escapeReplacement("$prefix$quote$rewritten$quote")
        }
    }

    private fun rewriteSpecifier(spec: String): String {
        return rules.firstNotNullOfOrNull { it.apply(spec) } ?: spec
    }
}
