package com.mussonindustrial.ignition.embr.gateway

import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.mussonindustrial.ignition.embr.common.EmbrCommonContextExtension
import com.mussonindustrial.ignition.embr.common.EmbrCommonContextExtensionImpl

open class EmbrGatewayContextImpl(private val context: GatewayContext):
        EmbrGatewayContext,
        GatewayContext by context,
        EmbrCommonContextExtension by EmbrCommonContextExtensionImpl(context)