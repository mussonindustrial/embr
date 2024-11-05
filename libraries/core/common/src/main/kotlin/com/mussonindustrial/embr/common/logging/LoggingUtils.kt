package com.mussonindustrial.embr.common.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun <T : Any> T.getLogger(): Logger {
    return LoggerFactory.getLogger(javaClass)
}
