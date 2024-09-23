package com.mussonindustrial.embr.common

interface EmbrCommonContextExtension {
    fun getModuleSafe(moduleId: String): Any?

    fun <T> ifModule(
        moduleId: String,
        action: () -> T,
    ): T?
}
