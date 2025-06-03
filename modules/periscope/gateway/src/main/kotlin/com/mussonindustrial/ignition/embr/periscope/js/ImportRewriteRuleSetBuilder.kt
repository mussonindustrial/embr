package com.mussonindustrial.ignition.embr.periscope.js

class ImportRewriteRuleSetBuilder() {

    companion object {
        fun registry(block: ImportRewriteRuleSetBuilder.() -> Unit): List<ImportRewriteRule> {
            return ImportRewriteRuleSetBuilder().apply(block).build()
        }
    }

    private val rules = mutableListOf<ImportRewriteRule>()

    fun stripPrefix(prefix: String, rewrite: (String) -> String) {
        rules += ImportRewriteRule { spec ->
            if (!spec.startsWith(prefix)) return@ImportRewriteRule null
            rewrite(spec.removePrefix(prefix))
        }
    }

    fun preserve(prefix: String) {
        rules += ImportRewriteRule { spec ->
            if (!spec.startsWith(prefix)) return@ImportRewriteRule null
            spec
        }
    }

    fun regex(pattern: Regex, rewrite: (MatchResult) -> String) {
        rules += ImportRewriteRule { spec ->
            val match = pattern.matchEntire(spec) ?: return@ImportRewriteRule null
            rewrite(match)
        }
    }

    fun build(): List<ImportRewriteRule> = rules
}
