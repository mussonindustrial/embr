package com.mussonindustrial.embr.common.logging

import com.inductiveautomation.ignition.common.util.LoggerEx
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun <T : Any> T.getLogger(): Logger {
    return LoggerFactory.getLogger(javaClass)
}

fun <T : Any> T.getLoggerEx(keys: Map<String, Any> = mapOf()): LoggerEx {
    return LoggerEx.newBuilder()
        .mdcContext(*keys.flatMap { (key, value) -> listOf(key, value) }.toTypedArray())
        .build(javaClass)
}
