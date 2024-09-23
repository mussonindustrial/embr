package com.mussonindustrial.embr.designer

import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.mussonindustrial.embr.common.EmbrCommonContextExtension

interface EmbrDesignerContext :
    DesignerContext, EmbrCommonContextExtension, EmbrDesignerContextExtension
