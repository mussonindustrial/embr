package com.mussonindustrial.embr.client

import com.inductiveautomation.ignition.client.model.ClientContext
import com.mussonindustrial.embr.common.EmbrCommonContextExtension
import com.mussonindustrial.embr.common.EmbrCommonContextExtensionImpl

open class EmbrClientContextImpl(private val context: ClientContext) :
    EmbrClientContext,
    ClientContext by context,
    EmbrCommonContextExtension by EmbrCommonContextExtensionImpl(context)
