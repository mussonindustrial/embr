package com.mussonindustrial.ignition.embr.periscope.js

fun interface ImportRewriteRule {
    fun apply(spec: String): String?
}
