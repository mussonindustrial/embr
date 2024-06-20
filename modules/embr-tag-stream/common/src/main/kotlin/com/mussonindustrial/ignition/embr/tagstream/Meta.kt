package com.mussonindustrial.ignition.embr.tagstream

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Meta {
    const val MODULE_ID = "com.mussonindustrial.embr.tagstream"
    const val SHORT_MODULE_ID = "embr-tag-stream"
    const val URL_ALIAS = "/embr/tag/stream"
}

fun <T : Any> T.getLogger(): Logger {
    return LoggerFactory.getLogger(javaClass)
}

fun <T : Any> T.getPrivateProperty(variableName: String): Any? {
    return javaClass.getDeclaredField(variableName).let { field ->
        field.isAccessible = true
        return@let field.get(this)
    }
}