package com.mussonindustrial.ignition.embr.designer

import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.mussonindustrial.ignition.embr.common.EmbrCommonContextExtension

interface EmbrDesignerContext:
    DesignerContext,
    EmbrCommonContextExtension,
    EmbrDesignerContextExtension