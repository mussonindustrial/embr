package com.mussonindustrial.embr.gateway

import com.mussonindustrial.embr.common.EmbrModuleMeta

interface EmbrGatewayContextExtension {
    fun isModuleInstalled(module: EmbrModuleMeta): Boolean
}
