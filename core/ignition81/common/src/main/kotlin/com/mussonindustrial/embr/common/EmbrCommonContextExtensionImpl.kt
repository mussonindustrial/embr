package com.mussonindustrial.embr.common

import com.inductiveautomation.ignition.common.model.CommonContext

class EmbrCommonContextExtensionImpl(val context: CommonContext) : EmbrCommonContextExtension {
    override fun getModuleSafe(moduleId: String): Any? {
        return try {
            context.getModule(moduleId)
        } catch (_: Throwable) {
            false
        }
    }

    override fun <T> ifModule(
        moduleId: String,
        action: () -> T,
    ): T? {
        if (getModuleSafe(moduleId) != null) {
            return action()
        }
        return null
    }
}
