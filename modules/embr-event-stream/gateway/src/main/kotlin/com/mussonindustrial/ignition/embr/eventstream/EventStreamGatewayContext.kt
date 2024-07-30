package com.mussonindustrial.ignition.embr.eventstream

import com.inductiveautomation.ignition.common.execution.ExecutionManager
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.inductiveautomation.ignition.gateway.user.UserSourceProfile
import com.inductiveautomation.perspective.common.PerspectiveModule
import com.inductiveautomation.perspective.gateway.api.PerspectiveContext
import com.mussonindustrial.ignition.embr.eventstream.streams.EventStreamManager
import com.mussonindustrial.ignition.embr.eventstream.tags.SystemTagsProvider
import com.mussonindustrial.ignition.embr.gateway.EmbrGatewayContext
import com.mussonindustrial.ignition.embr.gateway.EmbrGatewayContextImpl
import com.mussonindustrial.ignition.embr.servlets.ModuleServletManager
import com.mussonindustrial.ignition.embr.tagstream.Meta
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

data class EventStreamGatewayContext(val context: GatewayContext) : EmbrGatewayContext by EmbrGatewayContextImpl(context) {
    companion object {
        lateinit var instance: EventStreamGatewayContext
    }

    val systemTagsProvider = SystemTagsProvider(context.tagManager)
    val eventStreamManager = EventStreamManager(this)
    val servletManager = ModuleServletManager(context.webResourceManager, Meta.urlAlias)
    val perspectiveContext: PerspectiveContext?
    val userSourceProfile: UserSourceProfile = context.userSourceManager.getProfile("event-stream")
    val eventStreamExecutionManager: ExecutionManager =
        context.createExecutionManager(
            "Embr EventStream",
            3,
            object :
                ThreadFactory {
                private val counter = AtomicInteger(0)

                override fun newThread(r: Runnable): Thread = Thread(null, r, "embr-event-stream-executor-${counter.incrementAndGet()}")
            },
        )

    init {
        instance = this
        perspectiveContext =
            ifModule(PerspectiveModule.MODULE_ID) {
                PerspectiveContext.get(context)
            }
    }
}
