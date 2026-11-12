package com.mussonindustrial.ignition.embr.periscope.js

import java.util.concurrent.CompletableFuture

interface DesignerWebBridge {
    fun <T> invoke(
        service: String,
        method: String,
        payload: String,
        parse: (String) -> T,
    ): CompletableFuture<T>
}
