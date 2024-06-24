package com.mussonindustrial.ignition.embr.gateway

import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.mussonindustrial.ignition.embr.common.EmbrCommonContextExtension

interface EmbrGatewayContext:
    GatewayContext,
    EmbrCommonContextExtension,
    EmbrGatewayContextExtension