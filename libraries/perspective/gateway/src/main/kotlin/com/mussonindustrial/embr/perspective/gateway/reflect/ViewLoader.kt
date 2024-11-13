package com.mussonindustrial.embr.perspective.gateway.reflect

import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.util.LogUtil
import com.inductiveautomation.perspective.gateway.api.ViewInstanceId
import com.inductiveautomation.perspective.gateway.model.PageModel
import com.inductiveautomation.perspective.gateway.model.ViewModel
import com.mussonindustrial.embr.common.reflect.getSuperPrivateMethod
import java.util.*
import java.util.concurrent.CompletableFuture

class ViewLoader(page: PageModel) {

    private val _handlers = page.getHandlers()
    private val _startView =
        _handlers.getSuperPrivateMethod(
            "startView",
            String::class.java,
            String::class.java,
            Long::class.java,
            JsonObject::class.java
        )
    private val _findView = _handlers.getSuperPrivateMethod("findView", ViewInstanceId::class.java)
    private val log = LogUtil.getModuleLogger("embr-periscope", "ViewLoader")
    private val queue = page.session.queue()

    @Suppress("UNCHECKED_CAST")
    fun findView(viewId: ViewInstanceId): CompletableFuture<Optional<ViewModel>> {
        return _findView.invoke(_handlers, viewId) as CompletableFuture<Optional<ViewModel>>
    }

    fun startView(
        resourcePath: String,
        mountPath: String,
        birthDate: Long,
        params: JsonObject
    ): CompletableFuture<Optional<ViewModel>> {
        _startView.invoke(_handlers, resourcePath, mountPath, birthDate, params)

        val viewInstanceId = ViewInstanceId(resourcePath, mountPath)
        val maybeView = CompletableFuture<Optional<ViewModel>>()

        fun tryLoad(result: CompletableFuture<Optional<ViewModel>>) {
            findView(viewInstanceId)
                .thenApplyAsync(
                    { viewModel ->
                        viewModel.ifPresentOrElse(
                            {
                                log.trace("View ${viewInstanceId.id} loaded.")
                                result.complete(viewModel)
                            },
                            {
                                if (!maybeView.isCancelled) {
                                    queue.submit {
                                        log.trace(
                                            "View ${viewInstanceId.id} not found, getting back in queue..."
                                        )
                                        tryLoad(result)
                                    }
                                }
                            }
                        )
                    },
                    queue::submit
                )
        }
        tryLoad(maybeView)
        return maybeView
    }

    fun findOrStartView(
        resourcePath: String,
        mountPath: String,
        birthDate: Long,
        params: JsonObject
    ): CompletableFuture<Optional<ViewModel>> {
        val viewInstanceId = ViewInstanceId(resourcePath, mountPath)
        val startedView =
            findView(viewInstanceId).thenCompose {
                if (it.isPresent) {
                    CompletableFuture.completedFuture(it)
                } else {
                    startView(resourcePath, mountPath, birthDate, params)
                }
            }
        return startedView
    }
}
