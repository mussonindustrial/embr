package com.mussonindustrial.ignition.embr.periscope.js

interface JavaScriptImportRewriter {

    val rules: List<ImportRewriteRule>

    fun rewrite(source: String): String {
        return rewriteModule(source).source
    }

    fun rewriteModule(source: String): RewrittenModule {
        val output = StringBuilder()
        val imports = mutableListOf<IntRange>()
        var position = 0

        JavaScriptImportScanner(source).scan().forEach { found ->
            output.append(source, position, found.range.first)

            val start = output.length
            output.append(rewriteSpecifier(found.specifier))
            imports += start until output.length

            position = found.range.last + 1
        }
        output.append(source, position, source.length)

        return RewrittenModule(output.toString(), imports)
    }

    private fun rewriteSpecifier(spec: String): String {
        return rules.firstNotNullOfOrNull { it.apply(spec) } ?: spec
    }
}
