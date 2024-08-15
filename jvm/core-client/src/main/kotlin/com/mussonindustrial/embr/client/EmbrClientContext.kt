package com.mussonindustrial.embr.client

import com.inductiveautomation.ignition.client.model.ClientContext
import com.mussonindustrial.embr.common.EmbrCommonContextExtension
import com.mussonindustrial.embr.designer.EmbrClientContextExtension

interface EmbrClientContext :
    ClientContext,
    EmbrCommonContextExtension,
    EmbrClientContextExtension
