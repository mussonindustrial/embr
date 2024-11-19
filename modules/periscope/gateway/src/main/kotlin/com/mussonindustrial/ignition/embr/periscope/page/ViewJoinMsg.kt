package com.mussonindustrial.ignition.embr.periscope.page

import com.inductiveautomation.ignition.common.gson.JsonObject

class ViewJoinMsg(event: JsonObject) {
    companion object {
        const val PROTOCOL: String = "view-join"
    }

    val resourcePath: String = event.get("resourcePath")?.asString ?: ""
    val mountPath: String = event.get("mountPath")?.asString ?: ""
    val birthDate: Long = event.get("birthDate")?.asLong ?: 0
}
