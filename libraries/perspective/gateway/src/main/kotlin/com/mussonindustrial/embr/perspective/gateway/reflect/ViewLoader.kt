package com.mussonindustrial.embr.perspective.gateway.reflect

import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.util.ExecutionQueue
import com.inductiveautomation.perspective.gateway.api.ViewInstanceId
import com.inductiveautomation.perspective.gateway.model.PageModel
import com.inductiveautomation.perspective.gateway.model.ViewModel
import com.mussonindustrial.embr.common.reflect.getSuperPrivateMethod
import com.mussonindustrial.embr.common.reflect.getSuperPrivateProperty
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.time.TimeSource

class ViewLoader(page: PageModel) {

    private val _handlers = page.getSuperPrivateProperty("handlers")
    private val _startView =
        _handlers.getSuperPrivateMethod(
            "startView",
            String::class.java,
            String::class.java,
            Long::class.java,
            JsonObject::class.java
        )
    private val _findView = _handlers.getSuperPrivateMethod("findView", ViewInstanceId::class.java)

    fun startView(viewPath: String, mountPath: String, birthDate: Long, params: JsonObject) {
        _startView.invoke(_handlers, viewPath, mountPath, birthDate, params)
    }

    @Suppress("UNCHECKED_CAST")
    fun findView(viewId: ViewInstanceId): CompletableFuture<Optional<ViewModel>> {
        return _findView.invoke(_handlers, viewId) as CompletableFuture<Optional<ViewModel>>
    }

    fun waitForView(
        viewInstanceId: ViewInstanceId,
        queue: ExecutionQueue,
        waitLimitMs: Int
    ): CompletableFuture<Optional<ViewModel>> {

        val maybeView: CompletableFuture<Optional<ViewModel>> = CompletableFuture()
        val startTime = TimeSource.Monotonic.markNow()

        fun tryLoad(result: CompletableFuture<Optional<ViewModel>>) {
            if (startTime.elapsedNow().inWholeMilliseconds > waitLimitMs) {
                return
            }

            findView(viewInstanceId)
                .thenApplyAsync(
                    { viewModel ->
                        viewModel.ifPresentOrElse(
                            { result.complete(viewModel) },
                            { queue.submit { tryLoad(result) } }
                        )
                    },
                    queue::submit
                )
        }
        tryLoad(maybeView)
        return maybeView
    }
}
