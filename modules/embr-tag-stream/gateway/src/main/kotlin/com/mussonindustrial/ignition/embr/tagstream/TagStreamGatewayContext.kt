package com.mussonindustrial.ignition.embr.tagstream

import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.mussonindustrial.ignition.embr.tagstream.servlets.ModuleServletManager
import com.mussonindustrial.ignition.embr.tagstream.tags.TagStreamManager

data class TagStreamGatewayContext(val context: GatewayContext): GatewayContext by context {

    val tagStreamManager = TagStreamManager(context.tagManager)
    val servletManager = ModuleServletManager(context.webResourceManager, "/${Meta.URL_ALIAS}")

}