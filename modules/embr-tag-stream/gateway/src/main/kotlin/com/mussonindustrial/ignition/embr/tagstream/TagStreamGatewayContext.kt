package com.mussonindustrial.ignition.embr.tagstream

import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.mussonindustrial.ignition.embr.gateway.EmbrGatewayContext
import com.mussonindustrial.ignition.embr.gateway.EmbrGatewayContextImpl
import com.mussonindustrial.ignition.embr.servlets.ModuleServletManager

data class TagStreamGatewayContext(val context: GatewayContext): EmbrGatewayContext by EmbrGatewayContextImpl(context) {

    companion object {
        lateinit var INSTANCE: TagStreamGatewayContext
    }
    init {
        INSTANCE = this
    }

    val tagStreamSystemTagsProvider = TagStreamSystemTagsProvider(context.tagManager)
    val tagStreamManager = TagStreamManager(this)
    val servletManager = ModuleServletManager(context.webResourceManager, Meta.urlAlias)

}