package com.mussonindustrial.ignition.embr.tagstream

import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.mussonindustrial.ignition.embr.servlets.ModuleServletManager

data class TagStreamGatewayContext(val context: GatewayContext): GatewayContext by context {
    val tagStreamSystemTagsProvider = TagStreamSystemTagsProvider(context.tagManager)
    val tagStreamManager = TagStreamManager(this)
    val servletManager = ModuleServletManager(context.webResourceManager, Meta.URL_ALIAS)
}