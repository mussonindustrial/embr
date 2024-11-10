package com.mussonindustrial.ignition.embr.periscope

import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.inductiveautomation.perspective.gateway.api.PerspectiveContext
import com.inductiveautomation.perspective.gateway.model.PageModel
import com.mussonindustrial.embr.gateway.EmbrGatewayContext
import com.mussonindustrial.embr.gateway.EmbrGatewayContextImpl
import com.mussonindustrial.ignition.embr.periscope.page.ViewLoaderNew
import java.util.WeakHashMap

class PeriscopeGatewayContext(private val context: GatewayContext) :
    EmbrGatewayContext by EmbrGatewayContextImpl(context) {
    companion object {
        lateinit var instance: PeriscopeGatewayContext
    }

    val perspectiveContext: PerspectiveContext

    init {
        instance = this
        perspectiveContext = PerspectiveContext.get(context)
    }

    private val viewLoaders = WeakHashMap<PageModel, ViewLoaderNew>()

    fun getViewLoader(pageModel: PageModel): ViewLoaderNew {
        viewLoaders[pageModel]?.apply {
            return this
        }

        val newViewLoader = ViewLoaderNew(pageModel)
        viewLoaders[pageModel] = newViewLoader
        return newViewLoader
    }
}
