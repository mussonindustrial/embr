package com.mussonindustrial.ignition.embr.tagstream.api

import com.inductiveautomation.ignition.common.gson.*
import com.inductiveautomation.ignition.common.tags.model.TagPath
import com.inductiveautomation.ignition.common.tags.paths.parser.TagPathParser
import com.mussonindustrial.ignition.embr.common.gson.JsonSerializable
import java.lang.reflect.Type

data class TagStreamSessionRequest(val auth: AuthRequest, val tagPaths: List<TagPath>) {

    companion object {
        val gsonAdapter = object : JsonSerializable<TagStreamSessionRequest> {
            override fun serialize(request: TagStreamSessionRequest, type: Type, serializationContext: JsonSerializationContext): JsonElement {
                return JsonObject().apply {
                    add("auth", serializationContext.serialize(request.auth))
                    add("tag_paths", JsonArray().apply { request.tagPaths.forEach{ add(it.toString()) } })
                }
            }

            override fun deserialize(element: JsonElement, type: Type, deserializationContext: JsonDeserializationContext): TagStreamSessionRequest {
                val json = element.asJsonObject
                return TagStreamSessionRequest(
                    deserializationContext.deserialize(json.get("auth"), AuthRequest::class.java),
                    json.getAsJsonArray("tag_paths").map { TagPathParser.parse(it.asString) },
                )
            }
        }

        val gson: Gson = GsonBuilder()
            .registerTypeAdapter(TagStreamSessionRequest::class.java, this.gsonAdapter)
            .registerTypeAdapter(AuthRequest::class.java, AuthRequest.gsonAdapter)
            .registerTypeAdapter(AnonymousAuthRequest::class.java, AnonymousAuthRequest.gsonAdapter)
            .registerTypeAdapter(BasicAuthRequest::class.java, BasicAuthRequest.gsonAdapter)
            .registerTypeAdapter(PerspectiveAuthRequest::class.java, PerspectiveAuthRequest.gsonAdapter)
            .create()
    }
}