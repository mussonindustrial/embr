package com.mussonindustrial.embr.client

import com.inductiveautomation.ignition.client.model.ClientContext
import com.mussonindustrial.embr.common.EmbrCommonContextExtension
import com.mussonindustrial.embr.designer.EmbrClientContextExtension

interface EmbrClientContext :
    ClientContext, EmbrCommonContextExtension, EmbrClientContextExtension {

    fun getModuleSafe(moduleId: String): Any? {
        return try {
            getModule(moduleId)
        } catch (_: Throwable) {
            null
        }
    }

    fun <T> ifModule(moduleId: String, action: () -> T): T? {
        if (getModuleSafe(moduleId) != null) {
            return action()
        }
        return null
    }
}
