package com.mussonindustrial.embr.designer

import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.mussonindustrial.embr.common.EmbrCommonContextExtension
import com.mussonindustrial.embr.common.EmbrCommonContextExtensionImpl

open class EmbrDesignerContextImpl(private val context: DesignerContext) :
    EmbrDesignerContext,
    DesignerContext by context,
    EmbrCommonContextExtension by EmbrCommonContextExtensionImpl(context)
