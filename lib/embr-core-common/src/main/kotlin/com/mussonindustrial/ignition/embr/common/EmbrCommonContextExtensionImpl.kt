package com.mussonindustrial.ignition.embr.common

import com.inductiveautomation.ignition.common.model.CommonContext

class EmbrCommonContextExtensionImpl(val context: CommonContext): EmbrCommonContextExtension {

    override fun requireModule(moduleId: String, run: () -> Unit) {
        try {
            if (context.getModule(moduleId) != null) {
                run()
            }
        } catch (_: Throwable) {
        }
    }

}