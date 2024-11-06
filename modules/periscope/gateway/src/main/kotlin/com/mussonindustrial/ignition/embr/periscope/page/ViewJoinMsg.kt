package com.mussonindustrial.ignition.embr.periscope.page

import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.perspective.gateway.api.ViewInstanceId
import com.inductiveautomation.perspective.gateway.model.MessageChannel
import com.inductiveautomation.perspective.gateway.model.PageModel
import com.mussonindustrial.embr.perspective.gateway.reflect.ViewLoader

class ViewJoinMsg {
    var resourcePath: String = ""
    var mountPath: String = ""
    var birthDate: Long = 0
    var params: JsonObject? = null

    fun instanceId(): ViewInstanceId {
        return ViewInstanceId(this.resourcePath, this.mountPath)
    }

    companion object {
        const val PROTOCOL: String = "view-join"

        fun joinOrStart(page: PageModel): (channel: MessageChannel, message: ViewJoinMsg) -> Unit {
            val viewLoader = ViewLoader(page)
            return { _: MessageChannel, message: ViewJoinMsg ->
                viewLoader.findView(message.instanceId()).thenAccept { maybeView ->
                    if (maybeView.isEmpty) {
                        viewLoader.startView(
                            message.resourcePath,
                            message.mountPath,
                            message.birthDate,
                            message.params ?: JsonObject()
                        )
                    }
                }
            }
        }
    }
}
