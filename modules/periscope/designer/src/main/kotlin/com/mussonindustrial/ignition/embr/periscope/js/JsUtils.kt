package com.mussonindustrial.ignition.embr.periscope.js

import com.mussonindustrial.embr.perspective.common.exceptions.JavaScriptException
import com.teamdev.jxbrowser.js.JsObject
import com.teamdev.jxbrowser.js.JsPromise
import java.util.concurrent.CompletableFuture

inline fun <reified T> JsObject.require(name: String): T {
    return property<T>(name).orElseThrow { IllegalStateException("Missing JS property '$name'") }
}

inline fun <reified T> JsObject.optional(name: String): T? {
    return property<T>(name).orElse(null)
}

fun <T> JsPromise.await(transform: (Array<Any>) -> T): T {
    val future = CompletableFuture<T>()

    then { value -> future.complete(transform(value)) }
        .catchError { args ->
            val error = args.firstOrNull()

            val exception =
                when (error) {
                    null -> JavaScriptException("Unknown JavaScript error")
                    is JsObject -> {
                        JavaScriptException(error.require<String>("message"))
                    }
                    is Throwable -> error
                    else -> JavaScriptException(error.toString())
                }

            future.completeExceptionally(exception)
        }

    return future.get()
}
