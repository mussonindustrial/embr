package com.mussonindustrial.embr.common

import com.inductiveautomation.ignition.common.model.CommonContext

open class EmbrCommonContextImpl(val context: CommonContext) :
    CommonContext by context,
    EmbrCommonContextExtension by EmbrCommonContextExtensionImpl(context)
