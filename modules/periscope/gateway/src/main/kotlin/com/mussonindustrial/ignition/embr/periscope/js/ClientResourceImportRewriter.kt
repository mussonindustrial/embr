package com.mussonindustrial.ignition.embr.periscope.js

import com.mussonindustrial.ignition.embr.periscope.js.ImportRewriteRuleSetBuilder.Companion.registry

class ClientResourceImportRewriter(val projectName: String, val hash: String) :
    JavaScriptImportRewriter {

    override val rules = registry {
        preserve("http://")
        preserve("https://")
        preserve("/")
        preserve("./")
        preserve("../")
        stripPrefix("system:") { "/data/embr-periscope/system-module/${hash}/$it" }
        stripPrefix("@/") { "/data/embr-periscope/client-resource/${projectName}/${hash}/$it" }
        stripPrefix("@webdev/") { "/main/system/webdev/${projectName}/$it" }
    }
}
