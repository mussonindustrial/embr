package com.mussonindustrial.ignition.embr.e2e.testing.ignition

data class IgnitionProject(val gatewayUrl: String, val project: String) {
    val perspective = PerspectiveProject(gatewayUrl, project)
}

data class PerspectiveProject(val gatewayUrl: String, val project: String) {
    fun pageUrl(path: String): String {
        return "${gatewayUrl}/data/perspective/client/$project/$path"
    }
}
