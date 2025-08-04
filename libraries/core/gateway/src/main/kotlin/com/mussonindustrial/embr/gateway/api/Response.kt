package com.mussonindustrial.embr.gateway.api

import com.inductiveautomation.ignition.common.gson.JsonElement
import com.inductiveautomation.ignition.common.gson.JsonObject
import jakarta.servlet.http.HttpServletResponse
import kotlin.text.Charsets.UTF_8

fun <T : HttpServletResponse> T.sendSuccess(data: JsonElement) {
    val json = JsonObject()
    json.addProperty("status", "success")
    json.add("data", data)

    apply {
        status = HttpServletResponse.SC_OK
        contentType = "application/json"
        characterEncoding = UTF_8.toString()
        writer.println(json.toString())
        writer.close()
    }
}

fun <T : HttpServletResponse> T.sendSuccess() {
    val json = JsonObject()
    json.addProperty("status", "success")

    apply {
        status = HttpServletResponse.SC_OK
        contentType = "application/json"
        characterEncoding = UTF_8.toString()
        writer.println(json.toString())
        writer.close()
    }
}

fun <T : HttpServletResponse> T.sendError(message: String) {
    val json = JsonObject()
    json.addProperty("status", "error")
    json.addProperty("message", message)

    apply {
        status = HttpServletResponse.SC_BAD_REQUEST
        contentType = "application/json"
        characterEncoding = UTF_8.toString()
        writer.println(json.toString())
        writer.close()
    }
}
