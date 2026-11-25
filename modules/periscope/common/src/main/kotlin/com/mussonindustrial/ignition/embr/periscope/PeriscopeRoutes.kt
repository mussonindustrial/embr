package com.mussonindustrial.ignition.embr.periscope

import com.mussonindustrial.ignition.embr.periscope.Meta.SHORT_MODULE_ID

object PeriscopeRoutes {
    private const val BASE = "/data/$SHORT_MODULE_ID"

    const val ASSETS = "/assets/*"
    const val CLIENT_BUNDLE = "/client-bundle/:project_name/:hash"
    const val CLIENT_RESOURCE = "/client-resource/:project_name/:hash/*"
    const val CLIENT_RESOURCE_HASH = "/client-resource/:project_name/hash.json"
    const val GLOBAL_MODULE = "/global-module/:hash/*"
    const val SYSTEM_MODULE = "/system-module/:hash/*"

    fun asset(name: String): String {
        return "$BASE/assets/$name"
    }

    fun clientResource(projectName: String, hash: String, path: String): String {
        return "$BASE/client-resource/$projectName/$hash/$path"
    }

    fun globalModule(hash: String, name: String): String {
        return "$BASE/global-module/$hash/$name"
    }

    fun systemModule(hash: String, name: String): String {
        return "$BASE/system-module/$hash/$name"
    }
}
