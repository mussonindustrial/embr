package com.mussonindustrial.ignition.embr.periscope.api

import com.inductiveautomation.ignition.common.TypeUtilities
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.mussonindustrial.embr.perspective.gateway.model.ThreadContext
import org.python.core.PyDictionary

class JavaScriptRunMsg(
    val id: String,
    private val function: String,
    private val args: PyDictionary?,
    private val threadContext: ThreadContext,
) {
    companion object {
        const val PROTOCOL: String = "periscope-js-run"
    }

    fun getPayload(): JsonObject {
        return JsonObject().apply {
            addProperty("id", id)
            addProperty("function", function)
            add("args", TypeUtilities.pyToGson(args))
            add(
                "context",
                JsonObject().apply {
                    threadContext.view.get()?.let {
                        add(
                            "view",
                            JsonObject().apply {
                                addProperty("id", it.id.id)
                                addProperty("mountPath", it.id.mountPath)
                                addProperty("resourcePath", it.id.resourcePath)
                            }
                        )
                    }

                    threadContext.page.get()?.let {
                        add("page", JsonObject().apply { addProperty("id", it.id) })
                    }
                }
            )
        }
    }
}
