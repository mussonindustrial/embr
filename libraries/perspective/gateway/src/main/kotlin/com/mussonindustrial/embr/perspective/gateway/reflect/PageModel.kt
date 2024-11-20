package com.mussonindustrial.embr.perspective.gateway.reflect

import com.inductiveautomation.perspective.gateway.model.PageModel
import com.inductiveautomation.perspective.gateway.session.MessageProtocolDispatcher
import com.mussonindustrial.embr.common.reflect.getSuperPrivateProperty

fun PageModel.getHandlers(): MessageProtocolDispatcher {
    return this.getSuperPrivateProperty("handlers") as MessageProtocolDispatcher
}
