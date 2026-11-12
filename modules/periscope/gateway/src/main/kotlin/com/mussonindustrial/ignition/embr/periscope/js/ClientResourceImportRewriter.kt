package com.mussonindustrial.ignition.embr.periscope.js

import com.mussonindustrial.ignition.embr.periscope.js.ImportRewriteRuleSetBuilder.Companion.registry
import com.mussonindustrial.ignition.embr.periscope.js.modules.GlobalModule

class ClientResourceImportRewriter(val projectName: String, val hash: String) :
    JavaScriptImportRewriter {

    override val rules = registry {
        preserve("http://")
        preserve("https://")
        preserve("/")
        preserve("./")
        preserve("../")
        exact(GlobalModule.all.map { it.name }) { "/data/embr-periscope/global-module/${hash}/$it" }
        stripPrefix("system:") { "/data/embr-periscope/system-module/${hash}/$it" }
        stripPrefix("@/") { "/data/embr-periscope/client-resource/${projectName}/${hash}/$it" }
        stripPrefix("@webdev/") { "/main/system/webdev/${projectName}/$it" }
    }
}
