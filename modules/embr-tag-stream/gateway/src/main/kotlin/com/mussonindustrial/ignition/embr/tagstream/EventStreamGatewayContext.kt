package com.mussonindustrial.ignition.embr.tagstream

import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.inductiveautomation.ignition.gateway.user.UserSourceProfile
import com.inductiveautomation.perspective.common.PerspectiveModule
import com.inductiveautomation.perspective.gateway.api.PerspectiveContext
import com.mussonindustrial.ignition.embr.gateway.EmbrGatewayContext
import com.mussonindustrial.ignition.embr.gateway.EmbrGatewayContextImpl
import com.mussonindustrial.ignition.embr.servlets.ModuleServletManager
import com.mussonindustrial.ignition.embr.tagstream.tags.SystemTagsProvider

data class EventStreamGatewayContext(val context: GatewayContext): EmbrGatewayContext by EmbrGatewayContextImpl(context) {

    companion object {
        lateinit var INSTANCE: EventStreamGatewayContext
    }
    val systemTagsProvider = SystemTagsProvider(context.tagManager)
    val eventStreamManager = EventStreamManager(this)
    val servletManager = ModuleServletManager(context.webResourceManager, Meta.urlAlias)
    val perspectiveContext: PerspectiveContext?
    val userSourceProfile: UserSourceProfile = context.userSourceManager.getProfile("tag-stream")

    init {
        INSTANCE = this
        perspectiveContext = ifModule(PerspectiveModule.MODULE_ID) {
            PerspectiveContext.get(context)
        }
    }

}