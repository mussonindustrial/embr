package com.mussonindustrial.ignition.embr.periscope.page

import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.perspective.gateway.api.ViewInstanceId

class ViewJoinMsg(event: JsonObject) {
    companion object {
        const val PROTOCOL: String = "view-join"
    }

    val resourcePath: String = event.get("resourcePath")?.asString ?: ""
    val mountPath: String = event.get("mountPath")?.asString ?: ""
    val birthDate: Long = event.get("birthDate")?.asLong ?: 0
    val params: JsonObject = event.get("params")?.asJsonObject ?: JsonObject()

    fun instanceId(): ViewInstanceId {
        return ViewInstanceId(this.resourcePath, this.mountPath)
    }
}
