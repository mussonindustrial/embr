package com.mussonindustrial.embr.perspective.gateway.reflect

import com.inductiveautomation.perspective.gateway.session.AbstractSession
import com.inductiveautomation.perspective.gateway.session.InternalSession
import com.inductiveautomation.perspective.gateway.session.MessageProtocolDispatcher
import com.mussonindustrial.embr.common.reflect.getPrivateMethod
import com.mussonindustrial.embr.common.reflect.getSuperPrivateProperty
import java.util.Optional

fun InternalSession.getHandlers(): MessageProtocolDispatcher? {
    if (this is AbstractSession<*>) {
        return this.getSuperPrivateProperty("handlers") as MessageProtocolDispatcher
    }

    val maybeDelegate =
        try {
            this.getPrivateMethod("getDelegate").invoke(this) as Optional<*>
        } catch (e: Exception) {
            return null
        }

    val delegate = maybeDelegate.orElse(null) ?: return null
    return (delegate as InternalSession).getHandlers()
}

fun MessageProtocolDispatcher.unregister(protocol: String) {
    val handlers = this.getSuperPrivateProperty("handlers") as MutableMap<*, *>
    handlers.remove(protocol)
}
