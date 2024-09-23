package com.mussonindustrial.embr.gateway

import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.mussonindustrial.embr.common.EmbrCommonContextExtension

interface EmbrGatewayContext :
    GatewayContext, EmbrCommonContextExtension, EmbrGatewayContextExtension
